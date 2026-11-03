package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.SaleOrderStatus;
import com.nczy.coalsales.constant.WeighTicketStatus;
import com.nczy.coalsales.dto.WeighTicketCancelDTO;
import com.nczy.coalsales.dto.WeighTicketGrossDTO;
import com.nczy.coalsales.dto.WeighTicketTareDTO;
import com.nczy.coalsales.entity.SaleOrder;
import com.nczy.coalsales.entity.SettlementItem;
import com.nczy.coalsales.entity.Stock;
import com.nczy.coalsales.entity.StockRecord;
import com.nczy.coalsales.entity.WeighTicket;
import com.nczy.coalsales.mapper.SettlementItemMapper;
import com.nczy.coalsales.mapper.StockMapper;
import com.nczy.coalsales.mapper.WeighTicketMapper;
import com.nczy.coalsales.security.LoginUser;
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

    public WeighTicketServiceImpl(
            SaleOrderService saleOrderService,
            StockMapper stockMapper,
            StockRecordService stockRecordService,
            SettlementItemMapper settlementItemMapper) {
        this.saleOrderService = saleOrderService;
        this.stockMapper = stockMapper;
        this.stockRecordService = stockRecordService;
        this.settlementItemMapper = settlementItemMapper;
    }


    // ============ 磅单号生成：BD + 年月日 + 3 位流水 ============
    private String genWeightNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<WeighTicket>()
                .likeRight(WeighTicket::getWeightNo, "BD" + date));
        return "BD" + date + String.format("%03d", count + 1);
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
        weighTicket.setStatus(WeighTicketStatus.TARED);
        updateById(weighTicket);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addGrossWeight(long id, WeighTicketGrossDTO dto) {
        // ① 判空
        WeighTicket weighTicket = getById(id);
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

        // ⑤ 反查订单
        SaleOrder order = saleOrderService.getById(weighTicket.getOrderId());
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




        weighTicket.setGrossWeight(dto.getGrossWeight());
        weighTicket.setGrossTime(LocalDateTime.now());
        weighTicket.setNetWeight(netWeight);
        weighTicket.setStatus(WeighTicketStatus.FINISHED);
        updateById(weighTicket);

        // ⑦ 累加订单已发运量（读-改-写）
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



    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("磅单作废")
    public void cancelWeighTicket(Long id, WeighTicketCancelDTO dto) {
        WeighTicket weighTicket = getById(id);
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

        // 更新磅单为已作废
        weighTicket.setStatus(WeighTicketStatus.CANCELLED);
        weighTicket.setCancelReason(dto.getCancelReason());
        updateById(weighTicket);

        // 只有「已完成(2)」的磅单才回退订单
        if (WeighTicketStatus.FINISHED.equals(oldStatus)) {
            // 1. 回退订单发运量
            SaleOrder order = saleOrderService.getById(weighTicket.getOrderId());
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
            orderUpdate.setStatus(newShipped.compareTo(BigDecimal.ZERO) == 0
                    ? SaleOrderStatus.WAIT_SHIP : SaleOrderStatus.PART_SHIPPED);
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
