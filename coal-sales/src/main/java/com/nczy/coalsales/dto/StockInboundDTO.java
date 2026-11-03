package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购入库请求参数
 */
@Data
public class StockInboundDTO {

    /** 煤种商品 id */
    @NotNull(message = "煤种商品不能为空")
    private Long productId;

    /** 入库数量（吨），必须大于 0 */
    @NotNull(message = "入库数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "入库数量必须大于 0")
    @Digits(integer = 9, fraction = 3, message = "入库数量最多3位小数")
    private BigDecimal changeQty;

    /** 备注，可空 */
    private String remark;
}
