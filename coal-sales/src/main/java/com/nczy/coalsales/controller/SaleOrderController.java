package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.SaleOrderDTO;
import com.nczy.coalsales.dto.SaleOrderUpdateDTO;
import com.nczy.coalsales.entity.SaleOrder;
import com.nczy.coalsales.entity.WeighTicket;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SaleOrderService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/orders")
public class SaleOrderController {

    private final SaleOrderService saleOrderService;

    public SaleOrderController(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
    }

    @GetMapping
    public Result<PageResult<SaleOrder>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long contractId,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        return Result.success(saleOrderService.pageOrder(current, size, status, customerId, contractId, keyword));
    }

    @GetMapping("/{id}")
    public Result<SaleOrder> getOrder(@PathVariable Long id,
                                      @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        return Result.success(saleOrderService.getOrder(id, loginUser));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid SaleOrderDTO dto,
                            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        saleOrderService.addOrder(dto, loginUser);
        return Result.success("添加订单成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid SaleOrderUpdateDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        saleOrderService.updateOrder(id, dto, loginUser);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        saleOrderService.deleteOrder(id, loginUser);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        saleOrderService.cancelOrder(id, loginUser);
        return Result.success("订单已取消", null);
    }

    @GetMapping("/available")
    public Result<List<SaleOrder>> available(@AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.WEIGHMAN);
        return Result.success(saleOrderService.available());
    }
}
