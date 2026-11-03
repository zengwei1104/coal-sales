package com.nczy.coalsales.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CoalProductDTO {

    /** 煤种/商品名称 */
    @NotBlank(message = "煤种/商品名称不能为空")
    private String productName;

    /** 规格（如 3-6 块、粉煤）—— 可空 */
    private String spec;

    /** 收到基低位发热量（kcal/kg） */
    @Digits(integer = 8, fraction = 0, message = "发热量整数位最多8位")
    private BigDecimal qnetAr;

    /** 硫分（%） */
    @Digits(integer = 4, fraction = 2, message = "硫分最多2位小数")
    private BigDecimal sulfur;

    /** 灰分（%） */
    @Digits(integer = 4, fraction = 2, message = "灰分最多2位小数")
    private BigDecimal ash;

    /** 产地 */
    private String origin;

    /** 基准价（元/吨） */
    @NotNull(message = "基准价不能为空")
    @DecimalMin(value = "0", message = "基准价不能为负")
    @Digits(integer = 12, fraction = 2, message = "基准价最多2位小数")
    private BigDecimal basePrice;

    /** 安全库存（吨），默认0 */
    @DecimalMin(value = "0", message = "安全库存不能为负")
    @Digits(integer = 9, fraction = 3, message = "安全库存最多3位小数")
    private BigDecimal safeStock;

    /** 状态：1 上架 / 0 下架，默认1 */
    private Integer status;

    /** 备注 */
    private String remark;
}
