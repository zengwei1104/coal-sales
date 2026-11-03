package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.PaymentCreateDTO;
import com.nczy.coalsales.dto.PaymentWriteOffDTO;
import com.nczy.coalsales.entity.Payment;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public Result<PageResult<Payment>> page(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long customerId,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        return Result.success(paymentService.pagePayment(page, size, customerId));
    }

    @PostMapping
    public Result<Long> create(@RequestBody @Valid PaymentCreateDTO dto,
                               @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.FINANCE);
        Long paymentId = paymentService.createPayment(dto);
        return Result.success("登记回款成功", paymentId);
    }

    @PostMapping("/{id}/write-off")
    public Result<Void> writeOff(@PathVariable Long id,
                                 @RequestBody @Valid PaymentWriteOffDTO dto,
                                 @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.FINANCE);
        paymentService.writeOff(id, dto);
        return Result.success("核销成功", null);
    }
}
