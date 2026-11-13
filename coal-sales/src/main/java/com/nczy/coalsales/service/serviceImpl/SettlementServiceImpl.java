package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.SettlementStatus;
import com.nczy.coalsales.constant.WeighTicketStatus;
import com.nczy.coalsales.dto.SettlementAuditDTO;
import com.nczy.coalsales.dto.SettlementCreateDTO;
import com.nczy.coalsales.entity.Contract;
import com.nczy.coalsales.entity.SaleOrder;
import com.nczy.coalsales.entity.Settlement;
import com.nczy.coalsales.entity.SettlementItem;
import com.nczy.coalsales.entity.WeighTicket;
import com.nczy.coalsales.entity.WriteOff;
import com.nczy.coalsales.mapper.SettlementItemMapper;
import com.nczy.coalsales.mapper.SettlementMapper;
import com.nczy.coalsales.mapper.WeighTicketMapper;
import com.nczy.coalsales.mapper.WriteOffMapper;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ContractService;
import com.nczy.coalsales.service.SaleOrderService;
import com.nczy.coalsales.service.SettlementService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SettlementServiceImpl extends ServiceImpl<SettlementMapper, Settlement>
        implements SettlementService {

    private final ContractService contractService;
    private final SaleOrderService saleOrderService;
    private final WeighTicketMapper weighTicketMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final WriteOffMapper writeOffMapper;

    public SettlementServiceImpl(ContractService contractService,
                                 SaleOrderService saleOrderService,
                                 WeighTicketMapper weighTicketMapper,
                                 SettlementItemMapper settlementItemMapper,
                                 WriteOffMapper writeOffMapper) {
        this.contractService = contractService;
        this.saleOrderService = saleOrderService;
        this.weighTicketMapper = weighTicketMapper;
        this.settlementItemMapper = settlementItemMapper;
        this.writeOffMapper = writeOffMapper;
    }

    // ============ 结算单号：JS + 年月日 + 3 位流水 ============
    private String genSettleNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "JS" + date;
        String maxNo = baseMapper.selectMaxSettleNo(prefix);
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

    // ============ 分页 ============
    @Override
    public PageResult<Settlement> pageSettlement(Long current, Long size,
                                                 Integer status, Long customerId) {
        LambdaQueryWrapper<Settlement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, Settlement::getStatus, status)
               .eq(customerId != null, Settlement::getCustomerId, customerId)
               .orderByDesc(Settlement::getCreateTime);
        Page<Settlement> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    // ============ 详情 ============
    @Override
    public Settlement getSettlement(Long id) {
        Settlement settlement = getById(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在或已被删除");
        }
        return settlement;
    }

    @Override
    public List<SettlementItem> getItems(Long settleId) {
        return settlementItemMapper.selectList(
                new LambdaQueryWrapper<SettlementItem>()
                        .eq(SettlementItem::getSettleId, settleId));
    }

    // ============ 生成结算单（核心） ============
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createSettlement(SettlementCreateDTO dto, LoginUser loginUser) {
        // ① 合同校验
        Contract contract = contractService.getById(dto.getContractId());
        if (contract == null) {
            throw new BusinessException("合同不存在或已被删除");
        }

        // ② 磅单校验
        if (dto.getTicketIds() == null || dto.getTicketIds().isEmpty()) {
            throw new BusinessException("请至少选择一张磅单");
        }

        // 去重并按 id 排序，统一加锁顺序，降低死锁概率
        List<Long> ticketIds = dto.getTicketIds().stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();

        List<WeighTicket> tickets = new ArrayList<>();
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;

        // 逐张锁定磅单，锁内校验状态与是否已结算
        for (Long ticketId : ticketIds) {
            WeighTicket ticket = weighTicketMapper.selectByIdForUpdate(ticketId);
            if (ticket == null) {
                throw new BusinessException("磅单不存在或已被删除，id=" + ticketId);
            }
            if (!WeighTicketStatus.FINISHED.equals(ticket.getStatus())) {
                throw new BusinessException("磅单 " + ticket.getWeightNo() + " 未完成，不可结算");
            }
            if (ticket.getOrderId() == null) {
                throw new BusinessException("磅单 " + ticket.getWeightNo() + " 无关联订单");
            }
            SaleOrder order = saleOrderService.getById(ticket.getOrderId());
            if (order == null || !contract.getId().equals(order.getContractId())) {
                throw new BusinessException("磅单 " + ticket.getWeightNo() + " 不属于该合同");
            }
            Long settled = settlementItemMapper.selectCount(
                    new LambdaQueryWrapper<SettlementItem>()
                            .eq(SettlementItem::getTicketId, ticketId));
            if (settled != null && settled > 0) {
                throw new BusinessException("磅单 " + ticket.getWeightNo() + " 已结算，不可重复结算");
            }
            tickets.add(ticket);
        }

        // ③ 组装结算单
        Settlement settlement = new Settlement();
        settlement.setSettleNo(genSettleNo());
        settlement.setContractId(contract.getId());
        settlement.setCustomerId(contract.getCustomerId());
        settlement.setStatus(SettlementStatus.WAIT_AUDIT);
        settlement.setTaxAmount(BigDecimal.ZERO);
        settlement.setTotalQty(BigDecimal.ZERO);
        settlement.setTotalAmount(BigDecimal.ZERO);
        save(settlement);

        // ④ 使用已锁定的磅单对象组装明细，不再重复查询
        for (WeighTicket ticket : tickets) {
            BigDecimal amount = ticket.getNetWeight()
                    .multiply(contract.getPrice())
                    .setScale(2, RoundingMode.HALF_UP);

            SettlementItem item = new SettlementItem();
            item.setSettleId(settlement.getId());
            item.setTicketId(ticket.getId());
            item.setProductId(ticket.getProductId());
            item.setNetWeight(ticket.getNetWeight());
            item.setPrice(contract.getPrice());
            item.setAmount(amount);
            settlementItemMapper.insert(item);

            totalQty = totalQty.add(ticket.getNetWeight());
            totalAmount = totalAmount.add(amount);
        }

        // ⑤ 回填汇总
        Settlement update = new Settlement();
        update.setId(settlement.getId());
        update.setTotalQty(totalQty.setScale(3, RoundingMode.HALF_UP));
        update.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
        updateById(update);
    }

    // ============ 审核 ============
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("审核结算单")
    public void audit(Long id, SettlementAuditDTO dto, LoginUser loginUser) {
        //
        Settlement settlement = baseMapper.selectByIdForUpdate(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在或已被删除");
        }
        if (!SettlementStatus.WAIT_AUDIT.equals(settlement.getStatus())) {
            throw new BusinessException("只有待审核的结算单才能审核");
        }
        // 谁创建谁不能审核（R33）
        if (settlement.getCreateBy() != null
                && settlement.getCreateBy().equals(loginUser.getUserId())) {
            throw new BusinessException("不能审核自己创建的结算单");
        }
        Settlement update = new Settlement();
        update.setAuditBy(loginUser.getUserId());
        update.setAuditTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(dto.getPass())) {
            update.setStatus(SettlementStatus.AUDITED);
        } else {
            // 驳回：保持待审核并记录意见，供销售员重提
            update.setStatus(SettlementStatus.WAIT_AUDIT);
            update.setRemark(dto.getOpinion());
        }
        // 2. 条件更新：只允许“待审核”状态被更新
        LambdaUpdateWrapper<Settlement> wrapper =
                new LambdaUpdateWrapper<Settlement>()
                        .eq(Settlement::getId, id)
                        .eq(Settlement::getStatus, SettlementStatus.WAIT_AUDIT);

        int rows = baseMapper.update(update, wrapper);
        if (rows != 1) {
            throw new BusinessException("结算单状态已变化，请刷新后重试");
        }
    }

    // ============ 作废 ============
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("作废结算单")
    public void cancelSettlement(Long id, LoginUser loginUser) {
        // 与核销共用行锁，避免核销过程中结算单被作废
        Settlement settlement = baseMapper.selectByIdForUpdate(id);
        if (settlement == null) {
            throw new BusinessException("结算单不存在或已被删除");
        }
        if (SettlementStatus.CANCELLED.equals(settlement.getStatus())) {
            throw new BusinessException("该结算单已作废，无需重复操作");
        }
        Long writeOffCount = writeOffMapper.selectCount(
                new LambdaQueryWrapper<WriteOff>().eq(WriteOff::getSettleId, id));
        if (writeOffCount != null && writeOffCount > 0) {
            throw new BusinessException(1002, "该结算单已核销，不可作废");
        }

        Settlement update = new Settlement();
        update.setStatus(SettlementStatus.CANCELLED);

        LambdaUpdateWrapper<Settlement> wrapper =
                new LambdaUpdateWrapper<Settlement>()
                        .eq(Settlement::getId, id)
                        .eq(Settlement::getStatus, settlement.getStatus());

        int rows = baseMapper.update(update, wrapper);
        if (rows != 1) {
            throw new BusinessException("结算单状态已变化，请刷新后重试");
        }

        // 释放磅单：删除结算明细，使磅单可再次结算
        settlementItemMapper.delete(
                new LambdaQueryWrapper<SettlementItem>()
                        .eq(SettlementItem::getSettleId, id));
    }

    // ============ 可核销结算单（回款核销用） ============
    @Override
    public List<Settlement> listWriteoffable(Long customerId) {
        List<Settlement> list = baseMapper.listWriteoffableByCustomer(customerId);
        // 剩余未收 = 总额 - 已核销
        for (Settlement s : list) {
            BigDecimal received = s.getReceivedAmount() == null ? BigDecimal.ZERO : s.getReceivedAmount();
            BigDecimal total = s.getTotalAmount() == null ? BigDecimal.ZERO : s.getTotalAmount();
            s.setReceivedAmount(received.setScale(2, RoundingMode.HALF_UP));
            s.setRemainingAmount(total.subtract(received).setScale(2, RoundingMode.HALF_UP));
        }
        return list;
    }
}
