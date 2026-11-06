package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 煤炭销量排行出参（按煤种商品聚合）
 */
@Data
public class CoalSalesRankDTO {

    private Long productId;          // 煤种商品 id

    private String productName;      // 煤种名称

    private BigDecimal totalQty;     // 销量（净重，吨）

    private BigDecimal totalAmount;  // 销售额（元）
}
