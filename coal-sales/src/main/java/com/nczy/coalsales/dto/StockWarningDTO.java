package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存预警出参（触发预警的煤种）
 */
@Data
public class StockWarningDTO {

    private Long productId;          // 煤种商品 id

    private String productName;      // 煤种名称

    private BigDecimal totalQty;     // 当前库存（吨）

    private BigDecimal safeStock;    // 安全库存（吨）
}
