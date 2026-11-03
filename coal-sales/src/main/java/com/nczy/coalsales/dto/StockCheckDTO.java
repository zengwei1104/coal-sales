package com.nczy.coalsales.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 盘点调整请求参数
 * <p>
 * changeQty 可正可负：正数盘盈（库存增加），负数盘亏（库存减少）。
 */
@Data
public class StockCheckDTO {

    /** 煤种商品 id */
    @NotNull(message = "煤种商品不能为空")
    private Long productId;

    /** 调整数量（吨），不能为 0：正数盘盈，负数盘亏 */
    @NotNull(message = "调整数量不能为空")
    @Digits(integer = 9, fraction = 3, message = "调整数量最多3位小数")
    private BigDecimal changeQty;

    /** 调整原因，必填 */
    @NotBlank(message = "调整原因不能为空")
    private String remark;
}
