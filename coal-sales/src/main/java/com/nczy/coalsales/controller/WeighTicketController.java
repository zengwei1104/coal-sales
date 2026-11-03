package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.WeighTicketCancelDTO;
import com.nczy.coalsales.dto.WeighTicketGrossDTO;
import com.nczy.coalsales.dto.WeighTicketTareDTO;
import com.nczy.coalsales.entity.WeighTicket;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.WeighTicketService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weigh-tickets")
public class WeighTicketController {

    private final WeighTicketService weighTicketService;

    public WeighTicketController(WeighTicketService weighTicketService){
        this.weighTicketService =weighTicketService;
    }

    @GetMapping
    public Result<PageResult<WeighTicket>> page(@RequestParam(defaultValue = "1") Long current,
                                                @RequestParam(defaultValue = "10") Long size,
                                                @RequestParam(required = false) Integer status,
                                                @RequestParam(required = false) Long orderId,
                                                @RequestParam(required = false) String plateNo,
                                                @AuthenticationPrincipal LoginUser loginUser){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.MANAGER,RoleConstant.WEIGHMAN);
        return Result.success(weighTicketService.page(current,size,status,orderId,plateNo));
    }
    @PostMapping
    public Result<Void> addWeighTicket(@AuthenticationPrincipal LoginUser loginUser,
                                       @RequestBody @Valid WeighTicketTareDTO dto){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.MANAGER,RoleConstant.WEIGHMAN);
        weighTicketService.addWeighTicket(dto,loginUser);
        return Result.success("添加成功",null);
    }
    @PutMapping("/{id}/tare")
    public Result<Void> addTareWeight(@PathVariable("id")long id,
                                      @AuthenticationPrincipal LoginUser loginUser,
                                      @RequestBody @Valid WeighTicketTareDTO dto){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.MANAGER,RoleConstant.WEIGHMAN);
        weighTicketService.addTareWeight(id,dto);
        return Result.success("录入成功",null);
    }

    @PutMapping("/{id}/gross")
    public  Result<Void> addGrossWeight(@PathVariable("id")long id,
                                        @AuthenticationPrincipal LoginUser loginUser,
                                        @RequestBody @Valid WeighTicketGrossDTO dto){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.MANAGER,RoleConstant.WEIGHMAN);
        weighTicketService.addGrossWeight(id,dto);
        return Result.success("录入成功",null);
    }

    @PostMapping("/{id}/cancel")
    public Result<Void> cancelWeighTicket(@PathVariable("id") Long id,
                                          @AuthenticationPrincipal LoginUser loginUser,
                                          @RequestBody @Valid WeighTicketCancelDTO dto){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN,RoleConstant.MANAGER,RoleConstant.WEIGHMAN);
        weighTicketService.cancelWeighTicket(id,dto);
        return Result.success("取消成功",null);
    }

    @GetMapping("/available")
    public Result<List<WeighTicket>> available(@AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.WEIGHMAN);
        return Result.success(weighTicketService.available());
    }

    @GetMapping("/settleable")
    public Result<List<WeighTicket>> settleable(@RequestParam Long contractId,
                                                @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER);
        return Result.success(weighTicketService.settleable(contractId));
    }


}
