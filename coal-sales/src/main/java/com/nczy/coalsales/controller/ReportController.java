package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.CoalSalesRankDTO;
import com.nczy.coalsales.dto.CustomerRankDTO;
import com.nczy.coalsales.dto.PaymentRateDTO;
import com.nczy.coalsales.dto.SalesTrendDTO;
import com.nczy.coalsales.dto.StockWarningDTO;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.ReportService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /*
     * 销售趋势（按结算审核日聚合）
     * GET /api/report/sales-trend?startDate=2026-10-01&endDate=2026-11-06
     */
    @GetMapping("/sales-trend")
    public Result<List<SalesTrendDTO>> salesTrend(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        return Result.success(reportService.salesTrend(startDate, endDate));
    }

    /*
     * 煤炭销量排行（按煤种商品聚合）
     * GET /api/report/coal-rank?startDate=2026-10-01&endDate=2026-11-06
     */
    @GetMapping("/coal-rank")
    public Result<List<CoalSalesRankDTO>> coalSalesRank(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        return Result.success(reportService.coalSalesRank(startDate, endDate));
    }
    /*
      煤炭销量排行（按煤种商品聚合）
      GET /api/report/customer-rank?startDate=2026-10-01&endDate=2026-11-06
     */
    @GetMapping("/customer-rank")
    public Result<List<CustomerRankDTO>> customerRank(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal LoginUser loginUser){
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER,
                RoleConstant.SALES, RoleConstant.FINANCE);
        return Result.success(reportService.customerRank(startDate,endDate));
    }

    /*
     * 回款率统计
     * GET /api/reports/payment-rate?startDate=2026-10-01&endDate=2026-11-06
     */
    @GetMapping("/payment-rate")
    public Result<PaymentRateDTO> paymentRate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.FINANCE);
        return Result.success(reportService.paymentRate(startDate, endDate));
    }

    /*
     * 库存预警
     * GET /api/reports/stock-warning
     */
    @GetMapping("/stock-warning")
    public Result<List<StockWarningDTO>> stockWarning(
            @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN, RoleConstant.MANAGER, RoleConstant.STOCK);
        return Result.success(reportService.stockWarning());
    }
}
