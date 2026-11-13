package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.SettlementStatus;
import com.nczy.coalsales.dto.PaymentCreateDTO;
import com.nczy.coalsales.dto.PaymentWriteOffDTO;
import com.nczy.coalsales.dto.WriteOffItemDTO;
import com.nczy.coalsales.entity.Customer;
import com.nczy.coalsales.entity.Payment;
import com.nczy.coalsales.entity.Settlement;
import com.nczy.coalsales.entity.WriteOff;
import com.nczy.coalsales.mapper.CustomerMapper;
import com.nczy.coalsales.mapper.PaymentMapper;
import com.nczy.coalsales.mapper.SettlementMapper;
import com.nczy.coalsales.mapper.WriteOffMapper;
import com.nczy.coalsales.service.PaymentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment>
        implements PaymentService {

    private final CustomerMapper customerMapper;
    private final SettlementMapper settlementMapper;
    private final WriteOffMapper writeOffMapper;

    public PaymentServiceImpl(CustomerMapper customerMapper,
                              SettlementMapper settlementMapper,
                              WriteOffMapper writeOffMapper) {
        this.customerMapper = customerMapper;
        this.settlementMapper = settlementMapper;
        this.writeOffMapper = writeOffMapper;
    }

    private BigDecimal sumAmounts(List<BigDecimal> amounts) {
        return amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ============ 回款单号：HK + 年月日 + 3 位流水 ============
    private String genPaymentNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "HK" + date;
        String maxNo = baseMapper.selectMaxPaymentNo(prefix);
        int nextSeq = nextSeq(maxNo, prefix);
        return prefix + String.format("%03d", nextSeq);
    }

    // 从当天最大编号解析流水号并 +1，无记录则从 1 开始
    private int nextSeq(String maxNo, String prefix) {
        if (maxNo != null && maxNo.startsWith(prefix) && maxNo.length() > prefix.length()) {
            try {
                return Integer.parseInt(maxNo.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {
                // 编号格式异常时回退为 1
            }
        }
        return 1;
    }

    @Override
    public PageResult<Payment> pagePayment(Long current, Long size, Long customerId) {
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(customerId != null, Payment::getCustomerId, customerId)
               .orderByDesc(Payment::getCreateTime);
        Page<Payment> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Long createPayment(PaymentCreateDTO dto) {
        Customer customer = customerMapper.selectById(dto.getCustomerId());
        if (customer == null) {
            throw new BusinessException("客户不存在或已被删除");
        }

        Payment payment = new Payment();
        payment.setPaymentNo(genPaymentNo());
        payment.setCustomerId(dto.getCustomerId());
        payment.setAmount(dto.getAmount().setScale(2, RoundingMode.HALF_UP));
        payment.setPayType(dto.getPayType() == null ? 1 : dto.getPayType());
        payment.setPayTime(dto.getPayTime());
        payment.setRemark(dto.getRemark());
        save(payment);
        return payment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void writeOff(Long paymentId, PaymentWriteOffDTO dto) {
        // ① 锁定回款，保证同一笔回款的核销操作串行执行
        Payment payment = baseMapper.selectByIdForUpdate(paymentId);
        if (payment == null) {
            throw new BusinessException("回款记录不存在或已被删除");
        }

        // ② 校验本次核销总额及重复结算单
        BigDecimal requestTotal = BigDecimal.ZERO;
        Set<Long> settleIds = new HashSet<>();
        for (WriteOffItemDTO item : dto.getItems()) {
            if (item == null || item.getSettleId() == null) {
                throw new BusinessException("核销结算单不能为空");
            }
            if (item.getAmount() == null || item.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("核销金额必须大于 0");
            }
            if (!settleIds.add(item.getSettleId())) {
                throw new BusinessException("同一结算单不能在本次核销中重复出现");
            }
            requestTotal = requestTotal.add(item.getAmount());
        }

        BigDecimal usedAmount = sumAmounts(writeOffMapper.listAmountsByPaymentId(paymentId));
        BigDecimal availableAmount = payment.getAmount().subtract(usedAmount);
        if (requestTotal.compareTo(availableAmount) > 0) {
            throw new BusinessException("核销金额合计超过回款剩余可核销金额");
        }

        // 按结算单 id 排序后依次锁定，降低多笔核销并发时的死锁概率
        List<WriteOffItemDTO> items = new ArrayList<>(dto.getItems());
        items.sort(Comparator.comparing(WriteOffItemDTO::getSettleId));

        for (WriteOffItemDTO item : items) {
            Settlement settlement = settlementMapper.selectByIdForUpdate(item.getSettleId());
            if (settlement == null) {
                throw new BusinessException("结算单不存在或已被删除，id=" + item.getSettleId());
            }
            if (!SettlementStatus.AUDITED.equals(settlement.getStatus())) {
                throw new BusinessException("结算单 " + settlement.getSettleNo() + " 未审核，不可核销");
            }
            if (!payment.getCustomerId().equals(settlement.getCustomerId())) {
                throw new BusinessException("结算单 " + settlement.getSettleNo() + " 不属于该回款客户");
            }

            BigDecimal receivedAmount = sumAmounts(
                    writeOffMapper.listAmountsBySettleId(item.getSettleId()));
            BigDecimal remainingAmount = settlement.getTotalAmount().subtract(receivedAmount);
            if (item.getAmount().compareTo(remainingAmount) > 0) {
                throw new BusinessException("结算单 " + settlement.getSettleNo() + " 剩余未收金额不足");
            }

            WriteOff writeOff = new WriteOff();
            writeOff.setPaymentId(paymentId);
            writeOff.setSettleId(item.getSettleId());
            writeOff.setAmount(item.getAmount().setScale(2, RoundingMode.HALF_UP));
            writeOffMapper.insert(writeOff);
        }
    }

    @Override
    public BigDecimal getReceivable(Long customerId) {
        Customer customer = customerMapper.selectById(customerId);
        if (customer == null) {
            throw new BusinessException("客户不存在或已被删除");
        }
        return settlementMapper.sumReceivableByCustomerId(customerId)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
