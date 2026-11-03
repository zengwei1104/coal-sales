package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.PageResult;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.StockCheckDTO;
import com.nczy.coalsales.dto.StockInboundDTO;
import com.nczy.coalsales.entity.Stock;
import com.nczy.coalsales.entity.StockRecord;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.StockRecordService;
import com.nczy.coalsales.service.StockService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StockController {

    private final StockService stockService;
    private final StockRecordService stockRecordService;

    public StockController(StockService stockService, StockRecordService stockRecordService) {
        this.stockService = stockService;
        this.stockRecordService = stockRecordService;
    }

    @GetMapping("/stocks")
    public Result<List<Stock>> getStocks(
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.STOCK);
        return Result.success(stockService.getStocks());
    }

    @GetMapping("/stock-records")
    public Result<PageResult<StockRecord>> pageStockRecords(@RequestParam(defaultValue = "1") Long current,
                                                            @RequestParam(defaultValue = "10") Long size,
                                                            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.STOCK);
        return Result.success(stockRecordService.pageStockRecord(current, size));
    }

    @PostMapping("/stock-records/inbound")
    public Result<Void> inbound(@RequestBody @Valid StockInboundDTO dto,
                                @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.STOCK);
        stockService.inbound(dto, loginUser);
        return Result.success("入库成功", null);
    }

    @PostMapping("/stock-records/check")
    public Result<Void> checkStock(@RequestBody @Valid StockCheckDTO dto,
                                   @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.STOCK,RoleConstant.ADMIN);
        stockService.checkStock(dto, loginUser);
        return Result.success("盘点调整成功", null);
    }

}
