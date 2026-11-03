package com.nczy.coalsales.service.serviceImpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.constant.ContractStatus;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.constant.SaleOrderStatus;
import com.nczy.coalsales.dto.SaleOrderDTO;
import com.nczy.coalsales.dto.SaleOrderUpdateDTO;
import com.nczy.coalsales.entity.Contract;
import com.nczy.coalsales.entity.SaleOrder;
import com.nczy.coalsales.mapper.SaleOrderMapper;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ContractService;
import com.nczy.coalsales.service.SaleOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SaleOrderServiceImpl extends ServiceImpl<SaleOrderMapper, SaleOrder>
        implements SaleOrderService {

    private final ContractService contractService;
    private final SaleOrderMapper saleOrderMapper;

    public SaleOrderServiceImpl(ContractService contractService,SaleOrderMapper saleOrderMapper) {
        this.contractService = contractService;
        this.saleOrderMapper =saleOrderMapper;
    }

    // ============ 订单号生成：DD + 年月日 + 3 位流水 ============
    private String genOrderNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = count(new LambdaQueryWrapper<SaleOrder>()
                .likeRight(SaleOrder::getOrderNo, "DD" + date));
        return "DD" + date + String.format("%03d", count + 1);
    }

    // ============ 计算合同可下单上限 ============
    private BigDecimal calcUpperLimit(Contract contract) {
        BigDecimal shortRate = contract.getShortRate() == null
                ? BigDecimal.ZERO : contract.getShortRate();
        return contract.getTotalQty()
                .multiply(BigDecimal.ONE.add(shortRate.divide(new BigDecimal("100"))));
    }

    // ============ 归属校验：销售员只能操作自己的订单 ============
    private void checkOwner(SaleOrder order, LoginUser loginUser) {
        boolean isAdminOrManager = RoleConstant.ADMIN.equals(loginUser.getRoleId())
                || RoleConstant.MANAGER.equals(loginUser.getRoleId());
        if (!isAdminOrManager) {
            if (order.getCreateBy() == null
                    || !order.getCreateBy().equals(loginUser.getUserId())) {
                throw new BusinessException("无权操作该订单：这不是您创建的订单");
            }
        }
    }

    // ============ 分页查询 ============
    @Override
    public PageResult<SaleOrder> pageOrder(Long current, Long size,
                                           Integer status, Long customerId, Long contractId, String keyword) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(status != null, SaleOrder::getStatus, status)
               .eq(customerId != null, SaleOrder::getCustomerId, customerId)
               .eq(contractId != null, SaleOrder::getContractId, contractId)
               .orderByDesc(SaleOrder::getCreateTime);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SaleOrder::getOrderNo, keyword);
        }
        Page<SaleOrder> page = page(new Page<>(current, size), wrapper);
        return PageResult.of(page);
    }

    // ============ 详情 ============
    @Override
    public SaleOrder getOrder(Long id, LoginUser loginUser) {
        SaleOrder order = getById(id);
        if (order == null) {
            throw new BusinessException("订单不存在或已被删除");
        }
        checkOwner(order, loginUser);
        return order;
    }

    // ============ 新增（合同校验 + 剩余量 + 自动完成合同） ============
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addOrder(SaleOrderDTO dto, LoginUser loginUser) {
        // ① 合同校验
        Contract contract = contractService.getById(dto.getContractId());
        if (contract == null) {
            throw new BusinessException("合同不存在或已被删除");
        }
        if (!ContractStatus.EXECUTING.equals(contract.getStatus())) {
            throw new BusinessException("只有执行中的合同才能下单");
        }

        // ② 上限与剩余量
        BigDecimal upperLimit = calcUpperLimit(contract);//上限
        BigDecimal orderedTotal = saleOrderMapper.sumPlanQtyByContract(contract.getId());//未取消、未删除的订单的计划量
        BigDecimal remainQty = upperLimit.subtract(orderedTotal);//剩余可下单量

        // ③ 超限拒绝（R03），并告知剩余量
        if (dto.getPlanQty().compareTo(remainQty) > 0) {
            throw new BusinessException("下单量 " + dto.getPlanQty() + " 吨超过合同剩余可下单量 "
                    + remainQty + " 吨，请调整数量");
        }

        // ④ 组装订单并保存
        SaleOrder order = new SaleOrder();
        order.setContractId(contract.getId());
        order.setCustomerId(contract.getCustomerId());
        order.setProductId(contract.getProductId());
        order.setPlanQty(dto.getPlanQty());
        if (dto.getPrice() != null) {
            order.setPrice(dto.getPrice());
            order.setPriceRemark(dto.getPriceRemark());
        } else {
            order.setPrice(contract.getPrice());
        }
        order.setDeliveryDate(dto.getDeliveryDate());
        order.setPlateNo(dto.getPlateNo());
        order.setStatus(SaleOrderStatus.WAIT_SHIP);
        order.setShippedQty(new BigDecimal("0.000"));//初始化发运重量
        order.setSettledQty(new BigDecimal("0.000"));//初始化结算重量
        order.setOrderNo(genOrderNo());
        save(order);

        // ⑤ 判断合同是否用满 → 自动置"已完成"（R10）
        BigDecimal newTotal = orderedTotal.add(dto.getPlanQty());
        if (newTotal.compareTo(upperLimit) >= 0) {
            Contract update = new Contract();
            update.setId(contract.getId());
            update.setStatus(ContractStatus.FINISHED);
            contractService.updateById(update);
        }
    }

    // ============ 修改（仅待发运） ============
    @Override
    public void updateOrder(Long id, SaleOrderUpdateDTO dto, LoginUser loginUser) {
        SaleOrder order = getOrder(id, loginUser);
        if (!SaleOrderStatus.WAIT_SHIP.equals(order.getStatus())) {
            throw new BusinessException("只有待发运状态的订单才能修改");
        }
        SaleOrder update = new SaleOrder();
        update.setId(id);
        update.setPlanQty(dto.getPlanQty());
        update.setPrice(dto.getPrice());
        update.setPriceRemark(dto.getPriceRemark());
        update.setDeliveryDate(dto.getDeliveryDate());
        update.setPlateNo(dto.getPlateNo());
        updateById(update);
    }

    // ============ 删除（仅待发运） ============
    @Override
    public void deleteOrder(Long id, LoginUser loginUser) {
        SaleOrder order = getOrder(id, loginUser);
        if (!SaleOrderStatus.WAIT_SHIP.equals(order.getStatus())) {
            throw new BusinessException("只有待发运状态的订单才能删除");
        }
        removeById(id);
    }

    // ============ 取消（待发运→已取消，回退合同） ============
    @Override
    @Transactional(rollbackFor = Exception.class)
    @Log("取消订单")
    public void cancelOrder(Long id, LoginUser loginUser) {
        SaleOrder order = getOrder(id, loginUser);
        if (!SaleOrderStatus.WAIT_SHIP.equals(order.getStatus())) {
            throw new BusinessException("只有待发运状态的订单才能取消");
        }

        // 订单置已取消
        SaleOrder update = new SaleOrder();
        update.setId(id);
        update.setStatus(SaleOrderStatus.CANCELLED);
        updateById(update);

        // 合同若已"已完成"，释放量后回退"执行中"
        Contract contract = contractService.getById(order.getContractId());
        if (contract != null && ContractStatus.FINISHED.equals(contract.getStatus())) {
            Contract back = new Contract();
            back.setId(contract.getId());
            back.setStatus(ContractStatus.EXECUTING);
            contractService.updateById(back);
        }
    }

    @Override
    public List<SaleOrder> available() {
        return list(new LambdaQueryWrapper<SaleOrder>()
                .in(SaleOrder::getStatus,SaleOrderStatus.WAIT_SHIP,SaleOrderStatus.PART_SHIPPED)
                .orderByDesc(SaleOrder::getCreateTime));
    }
}
