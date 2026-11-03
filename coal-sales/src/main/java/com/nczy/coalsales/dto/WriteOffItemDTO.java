package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 单笔核销明细入参
 */
@Data
public class WriteOffItemDTO {

    @NotNull(message = "结算单不能为空")
    private Long settleId;

    @NotNull(message = "核销金额不能为空")
    @DecimalMin(value = "0.01", message = "核销金额必须大于 0")
    @Digits(integer = 12, fraction = 2, message = "核销金额最多 12 位整数且保留 2 位小数")
    private BigDecimal amount;
}
