package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.ContractStatus;
import com.nczy.coalsales.constant.SaleOrderStatus;
import com.nczy.coalsales.constant.WeighTicketStatus;
import com.nczy.coalsales.dto.WeighTicketCancelDTO;
import com.nczy.coalsales.dto.WeighTicketGrossDTO;
import com.nczy.coalsales.dto.WeighTicketTareDTO;
import com.nczy.coalsales.entity.*;
import com.nczy.coalsales.mapper.*;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ContractService;
import com.nczy.coalsales.service.SaleOrderService;
import com.nczy.coalsales.service.StockRecordService;
import com.nczy.coalsales.service.WeighTicketService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class WeighTicketServiceImpl extends ServiceImpl<WeighTicketMapper, WeighTicket> implements WeighTicketService {
    private final SaleOrderService saleOrderService;
    private final StockMapper stockMapper;
    private final StockRecordService stockRecordService;
    private final SettlementItemMapper settlementItemMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final  ContractMapper contractMapper;
    private  final ContractService contractService;

    public WeighTicketServiceImpl(
            SaleOrderService saleOrderService,
            StockMapper stockMapper,
            StockRecordService stockRecordService,
            SettlementItemMapper settlementItemMapper,
            SaleOrderMapper saleOrderMapper,
            ContractMapper contractMapper,
            ContractService contractService) {
        this.saleOrderService = saleOrderService;
        this.stockMapper = stockMapper;
        this.stockRecordService = stockRecordService;
        this.settlementItemMapper = settlementItemMapper;
        this.saleOrderMapper =saleOrderMapper;
        this.contractMapper =contractMapper;
        this.contractService =contractService;
    }


    // ============ 磅单号生成：BD + 年月日 + 3 位流水 ============
    private String genWeightNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BD" + date;
        String maxNo = baseMapper.selectMaxWeightNo(prefix);
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
    public PageResult<WeighTicket> page(Long current, Long size, Integer status,Long orderId, String plateNo) {
        LambdaQueryWrapper<WeighTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, WeighTicket::getStatus, status)
                .eq(orderId != null,WeighTicket::getOrderId,orderId)
                .like(StringUtils.hasText(plateNo), WeighTicket::getPlateNo, plateNo)
                .orderByDesc(WeighTicket::getCreateTime);   // 建议加个排序

        Page<WeighTicket> page = page(new Page<>(current, size), wrapper);  // ← 补上这行
        return PageResult.of(page);
    }

    @Override
    public void addWeighTicket(WeighTicketTareDTO dto, LoginUser loginUser) {
        // ① 订单校验
        SaleOrder saleOrder = saleOrderService.getById(dto.getOrderId());
        if (saleOrder == null) {
            throw new BusinessException("订单不存在或已被删除");
        }

        // ② 组装磅单
        WeighTicket weighTicket = new WeighTicket();
        weighTicket.setWeightNo(genWeightNo()); // 开单时生成磅单号
        weighTicket.setDriver(dto.getDriver());
        weighTicket.setOrderId(dto.getOrderId());
        weighTicket.setPlateNo(dto.getPlateNo());
        weighTicket.setTareSource(dto.getTareSource());
        weighTicket.setStatus(WeighTicketStatus.WAIT_TARE);
        weighTicket.setProductId(saleOrder.getProductId());
        weighTicket.setWeighmanId(loginUser.getUserId());

        save(weighTicket);
    }


    @Override
    public void addTareWeight(long id, WeighTicketTareDTO dto) {
        // ① 判空
        WeighTicket weighTicket = getById(id);
        if (weighTicket == null) {
            throw new BusinessException("磅单不存在或已被删除");
        }
        // ② 状态校验
        if (!WeighTicketStatus.WAIT_TARE.equals(weighTicket.getStatus())) {
            throw new BusinessException("只有待称皮重的磅单才能称皮重");
        }
        // ③ 皮重合法性
        if (dto.getTareWeight() == null || dto.getTareWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("皮重必须大于 0");
        }
        // ④ 更新（含状态、来源、时间）
        weighTicket.setTareWeight(dto.getTareWeight());
        weighTicket.setTareTime(LocalDateTime.now());
        weighTicket.setTareSource(dto.getTareSource());
        weighTicket.setStatus(WeighTicketStatus.TARED);
        updateById(weighTicket);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGrossWeight(long id, WeighTicketGrossDTO dto) {
        // ① 锁定磅单，后续状态判断必须基于锁内读取的最新数据
        WeighTicket weighTicket = baseMapper.selectByIdForUpdate(id);
        if (weighTicket == null) {
            throw new BusinessException("磅单不存在或已被删除");
        }

        if (!WeighTicketStatus.TARED.equals(weighTicket.getStatus())) {
            throw new BusinessException("只有已称皮重的磅单才能称毛重");
        }

        // ③ 毛重合法性：毛重必须 > 皮重（否则净重为负或 0）
        if (dto.getGrossWeight() == null
                || dto.getGrossWeight().compareTo(weighTicket.getTareWeight()) <= 0) {
            throw new BusinessException("毛重必须大于皮重");
        }

        // ④ 计算净重 = 毛重 − 皮重
        BigDecimal netWeight = dto.getGrossWeight().subtract(weighTicket.getTareWeight());

        // ⑤ 锁定订单，防止多张磅单并发完成时互相覆盖 shippedQty
        SaleOrder order = saleOrderMapper.selectByIdForUpdate(weighTicket.getOrderId());
        if (order == null) {
            throw new BusinessException("订单不存在或已被删除");
        }
        // 订单状态校验：只有待发运/部分发运才能发车
        if (!SaleOrderStatus.WAIT_SHIP.equals(order.getStatus())
                && !SaleOrderStatus.PART_SHIPPED.equals(order.getStatus())) {
            throw new BusinessException("订单当前状态不允许发运");
        }

        //库存流水
        BigDecimal changeQty= netWeight.negate();//出库取复数
        //锁定仓库
        Stock stock =stockMapper.selectByProductIdForUpdate(order.getProductId());
        if (stock == null) {
            throw new BusinessException("该煤种暂无库存记录，无法出库");
        }
        //计算出库流水
        BigDecimal beforeQty =stock.getTotalQty();
        BigDecimal afterQty = beforeQty.add(changeQty);

        int rows = stockMapper.changeStockQty(order.getProductId(), changeQty);
        if (rows != 1) {
            throw new BusinessException("库存不足，无法完成出库");
        }

        StockRecord record =new StockRecord();
        record.setProductId(order.getProductId());
        record.setSourceType("TICKET");
        record.setSourceId(weighTicket.getId());
        record.setChangeQty(changeQty);
        record.setBeforeQty(beforeQty);
        record.setAfterQty(afterQty);
        record.setRemark("磅单销售出库");
        record.setOperatorId(weighTicket.getWeighmanId());
        stockRecordService.save(record);

        // ⑥ 条件更新磅单：必须仍处于“已称皮重”，避免重复完成
        int ticketRows = baseMapper.finishWeighing(
                id, dto.getGrossWeight(), netWeight, LocalDateTime.now());
        if (ticketRows != 1) {
            throw new BusinessException("磅单状态已变化，请刷新后重试");
        }

        // ⑦ 累加订单已发运量（订单行已锁定，读-改-写安全）
        BigDecimal newShipped = order.getShippedQty().add(netWeight);
        SaleOrder orderUpdate = new SaleOrder();
        orderUpdate.setId(order.getId());
        orderUpdate.setShippedQty(newShipped);

        // ⑧ 判断订单状态流转：发满 → 已完成(3)，否则 → 部分发运(2)
        if (newShipped.compareTo(order.getPlanQty()) >= 0) {
            orderUpdate.setStatus(SaleOrderStatus.FINISHED);
        } else {
            orderUpdate.setStatus(SaleOrderStatus.PART_SHIPPED);
        }
        saleOrderService.updateById(orderUpdate);

        //  判断合同是否用满 → 自动置"已完成"（R10）
        Contract contract = contractMapper.selectById(order.getContractId());
        if (contract == null) {
            throw new BusinessException("合同不存在或已被删除");
        }

        BigDecimal shippedTotal = saleOrderMapper.sumShippedQtyByContract(order.getContractId());
        if (shippedTotal != null
                && contract.getTotalQty() != null
                && shippedTotal.compareTo(contract.getTotalQty()) >= 0) {
            boolean contractRows  = contractService.lambdaUpdate()
                    .eq(Contract::getId, contract.getId())
                    .eq(Contract::getStatus, ContractStatus.EXECUTING)
                    .set(Contract::getStatus, ContractStatus.FINISHED)
                    .update();
            // rows 为 0 说明合同状态已变化（并发下被改过），可忽略或记录日志
        }




    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("磅单作废")
    public void cancelWeighTicket(Long id, WeighTicketCancelDTO dto) {
        // ① 锁定磅单，重复作废请求会在锁释放后看到最新状态
        WeighTicket weighTicket = baseMapper.selectByIdForUpdate(id);
        if (weighTicket == null) {
            throw new BusinessException("磅单不存在或已被删除");
        }
        if (WeighTicketStatus.CANCELLED.equals(weighTicket.getStatus())) {
            throw new BusinessException("该磅单已作废，无需重复操作");
        }
        if (dto.getCancelReason() == null || dto.getCancelReason().trim().isEmpty()) {
            throw new BusinessException("作废原因不能为空");
        }

        // 已结算的磅单不可作废（R19）
        Long settled = settlementItemMapper.selectCount(
                new LambdaQueryWrapper<SettlementItem>()
                        .eq(SettlementItem::getTicketId, weighTicket.getId()));
        if (settled != null && settled > 0) {
            throw new BusinessException("该磅单已结算，不可作废");
        }

        Integer oldStatus = weighTicket.getStatus();

        // ② 条件更新为已作废，并要求状态仍与锁内读取值一致
        int ticketRows = baseMapper.cancelIfStatus(
                id, oldStatus, dto.getCancelReason().trim());
        if (ticketRows != 1) {
            throw new BusinessException("磅单状态已变化，请刷新后重试");
        }

        // 只有「已完成(2)」的磅单才回退订单
        if (WeighTicketStatus.FINISHED.equals(oldStatus)) {
            if (weighTicket.getNetWeight() == null) {
                throw new BusinessException("磅单净重数据异常，无法回退库存");
            }

            // 1. 锁定订单后回退发运量，防止覆盖其他磅单地并发更新
            SaleOrder order = saleOrderMapper.selectByIdForUpdate(weighTicket.getOrderId());
            if (order == null) {
                throw new BusinessException("订单不存在或已被删除");
            }
            BigDecimal newShipped = order.getShippedQty().subtract(weighTicket.getNetWeight());
            if (newShipped.compareTo(BigDecimal.ZERO) < 0) {
                newShipped = BigDecimal.ZERO;
            }
            SaleOrder orderUpdate = new SaleOrder();
            orderUpdate.setId(order.getId());
            orderUpdate.setShippedQty(newShipped);
            if (newShipped.compareTo(BigDecimal.ZERO) == 0) {
                orderUpdate.setStatus(SaleOrderStatus.WAIT_SHIP);
            } else if (newShipped.compareTo(order.getPlanQty()) >= 0) {
                orderUpdate.setStatus(SaleOrderStatus.FINISHED);
            } else {
                orderUpdate.setStatus(SaleOrderStatus.PART_SHIPPED);
            }
            saleOrderService.updateById(orderUpdate);

            // 2. 恢复库存（正数）
            BigDecimal changeQty = weighTicket.getNetWeight();

            Stock stock = stockMapper.selectByProductIdForUpdate(weighTicket.getProductId());
            if (stock == null) {
                throw new BusinessException("该煤种暂无库存记录");
            }
            BigDecimal beforeQty = stock.getTotalQty();
            BigDecimal afterQty = beforeQty.add(changeQty);

            int rows = stockMapper.changeStockQty(weighTicket.getProductId(), changeQty);
            if (rows != 1) {
                throw new BusinessException("库存恢复失败");
            }

            // 3. 写恢复库存流水
            StockRecord record = new StockRecord();
            record.setProductId(weighTicket.getProductId());
            record.setSourceType("TICKET");
            record.setSourceId(weighTicket.getId());
            record.setChangeQty(changeQty);
            record.setBeforeQty(beforeQty);
            record.setAfterQty(afterQty);
            record.setRemark("磅单作废，恢复库存");
            record.setOperatorId(weighTicket.getWeighmanId());
            stockRecordService.save(record);
        }


    }

    @Override
    public List<WeighTicket> available() {
        return list(new LambdaQueryWrapper<WeighTicket>()
                .eq(WeighTicket::getStatus, WeighTicketStatus.FINISHED)
                .notInSql(WeighTicket::getId,
                        "SELECT ticket_id FROM settlement_item WHERE ticket_id IS NOT NULL")
                .orderByDesc(WeighTicket::getCreateTime));
    }

    @Override
    public List<WeighTicket> settleable(Long contractId) {
        return baseMapper.settleable(contractId);
    }

}
