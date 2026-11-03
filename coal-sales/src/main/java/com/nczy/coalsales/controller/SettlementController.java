package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.SettlementAuditDTO;
import com.nczy.coalsales.dto.SettlementCreateDTO;
import com.nczy.coalsales.entity.Settlement;
import com.nczy.coalsales.entity.SettlementItem;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SettlementService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settlements")
public class SettlementController {

    private final SettlementService settlementService;

    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @GetMapping
    public Result<PageResult<Settlement>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long customerId,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        return Result.success(settlementService.pageSettlement(current, size, status, customerId));
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id,
                                              @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        Settlement settlement = settlementService.getSettlement(id);
        List<SettlementItem> items = settlementService.getItems(id);
        Map<String, Object> data = new HashMap<>();
        data.put("settlement", settlement);
        data.put("items", items);
        return Result.success(data);
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid SettlementCreateDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        settlementService.createSettlement(dto, loginUser);
        return Result.success("生成结算单成功", null);
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestBody @Valid SettlementAuditDTO dto,
                              @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.FINANCE, RoleConstant.ADMIN);
        settlementService.audit(id, dto, loginUser);
        return Result.success("审核完成", null);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        settlementService.cancelSettlement(id, loginUser);
        return Result.success("作废成功", null);
    }

    @GetMapping("/writeoffable")
    public Result<List<Settlement>> writeoffable(@RequestParam Long customerId,
                                                 @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.FINANCE);
        return Result.success(settlementService.listWriteoffable(customerId));
    }
}
