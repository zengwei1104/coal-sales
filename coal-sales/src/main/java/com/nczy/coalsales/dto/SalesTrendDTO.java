package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售趋势出参（按结算审核日聚合）
 */
@Data
public class SalesTrendDTO {

    private String date;            // 审核日 yyyy-MM-dd

    private BigDecimal settleQty;   // 当日结算净重（吨）

    private BigDecimal settleAmount;// 当日结算金额（元）

    private Long orderCount;        // 当日结算单笔数
}
