package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.CustomerDTO;
import com.nczy.coalsales.entity.Customer;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.CustomerService;
import com.nczy.coalsales.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final PaymentService paymentService;

    public CustomerController(CustomerService customerService, PaymentService paymentService) {
        this.customerService = customerService;
        this.paymentService = paymentService;
    }
      @GetMapping
      public Result<PageResult<Customer>> page(@RequestParam(defaultValue = "1") Long current,
                                               @RequestParam(defaultValue = "10") Long size,
                                               @AuthenticationPrincipal LoginUser loginUser){
          AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,RoleConstant.FINANCE,RoleConstant.SALES);
          return Result.success(customerService.pageCustomer(current,size));
      }

      @PostMapping
      public Result<Void> addCustomer(@RequestBody @Valid CustomerDTO dto,
                                      @AuthenticationPrincipal LoginUser loginUser){
          AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
        customerService.addCustomer(dto);
        return Result.success("添加客户成功",null);
      }
      @DeleteMapping("/{id}")
      public Result<Void> deleteCustomer(@PathVariable("id") Long id,
                                         @AuthenticationPrincipal LoginUser loginUser){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER);
        customerService.deleteCustomer(id);
        return Result.success("删除成功",null);
      }
      @GetMapping("/{id}/receivable")
      public Result<BigDecimal> getReceivable(@PathVariable("id") Long id,
                                              @AuthenticationPrincipal LoginUser loginUser) {
          AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES, RoleConstant.FINANCE);
          return Result.success(paymentService.getReceivable(id));
      }

      @GetMapping("/{id}")
      public  Result<Customer> getCustomer(@PathVariable("id") long id,
                                           @AuthenticationPrincipal LoginUser loginUser){
          AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES,RoleConstant.FINANCE);
        return Result.success("查询成功",customerService.getCustomer(id));
      }
      @PutMapping("/{id}")
      public  Result<Void> updateCustomer(@PathVariable("id") long id,
                                          @AuthenticationPrincipal LoginUser loginUser,
                                          @RequestBody @Valid CustomerDTO dto){
          AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.SALES);
          customerService.updateCustomer(id,dto,loginUser);
        return Result.success("修改成功",null);
      }
}
