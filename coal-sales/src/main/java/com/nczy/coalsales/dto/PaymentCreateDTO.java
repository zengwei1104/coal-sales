package com.nczy.coalsales.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 登记回款入参
 */
@Data
public class PaymentCreateDTO {

    @NotNull(message = "客户不能为空")
    private Long customerId;

    @NotNull(message = "回款金额不能为空")
    @DecimalMin(value = "0.01", message = "回款金额必须大于 0")
    @Digits(integer = 12, fraction = 2, message = "回款金额最多 12 位整数且保留 2 位小数")
    private BigDecimal amount;

    @Min(value = 1, message = "付款方式不正确")
    @Max(value = 3, message = "付款方式不正确")
    private Integer payType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime payTime;

    @Size(max = 200, message = "备注不能超过 200 个字符")
    private String remark;
}
