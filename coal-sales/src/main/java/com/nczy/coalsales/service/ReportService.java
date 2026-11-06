package com.nczy.coalsales.service;

import com.nczy.coalsales.dto.CoalSalesRankDTO;
import com.nczy.coalsales.dto.CustomerRankDTO;
import com.nczy.coalsales.dto.PaymentRateDTO;
import com.nczy.coalsales.dto.SalesTrendDTO;
import com.nczy.coalsales.dto.StockWarningDTO;

import java.util.List;

public interface ReportService {

    /** 销售趋势（按结算审核日聚合） */
    List<SalesTrendDTO> salesTrend(String startDate, String endDate);

    /** 煤炭销量排行（按煤种商品聚合） */
    List<CoalSalesRankDTO> coalSalesRank(String startDate, String endDate);

    List<CustomerRankDTO> customerRank(String startDate, String endDate);

    /** 回款率统计 */
    PaymentRateDTO paymentRate(String startDate, String endDate);

    /** 库存预警 */
    List<StockWarningDTO> stockWarning();
}
