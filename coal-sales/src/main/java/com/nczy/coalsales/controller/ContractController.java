package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.ContractDTO;
import com.nczy.coalsales.entity.Contract;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/contracts")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @GetMapping
    public Result<PageResult<Contract>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES,RoleConstant.FINANCE);
        return Result.success(contractService.pageContract(current, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<Contract> getContract(@PathVariable Long id,
                                        @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES,RoleConstant.FINANCE);
        return Result.success(contractService.getContract(id));
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid ContractDTO dto,
                            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        contractService.addContract(dto, loginUser);
        return Result.success("添加合同成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id,
                               @RequestBody @Valid ContractDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        contractService.updateContract(id, dto, loginUser);
        return Result.success("修改成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        contractService.deleteContract(id, loginUser);
        return Result.success("删除成功", null);
    }

    @PostMapping("/{id}/submit")
    public Result<Void> submit(@PathVariable Long id,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER);
        contractService.submit(id, loginUser);
        return Result.success("提交成功", null);
    }

    @PostMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id,
                              @RequestBody Map<String, Object> body,
                              @AuthenticationPrincipal LoginUser loginUser) {
        // 审核只允许销售经理（需求：销售经理负责合同审核）
        AuthUtil.checkRole(loginUser, RoleConstant.MANAGER,RoleConstant.SALES);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = (String) body.get("opinion");
        contractService.audit(id, pass, opinion, loginUser);
        return Result.success("审核完成", null);
    }
}
