package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ContractDTO {

    @NotNull(message = "客户不能为空")
    private Long customerId;

    @NotNull(message = "煤种商品不能为空")
    private Long productId;

    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;

    @NotNull(message = "约定总量不能为空")
    @DecimalMin(value = "0", message = "约定总量不能为负数")
    private BigDecimal totalQty;

    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0", message = "单价不能为负数")
    private BigDecimal price;

    private BigDecimal taxRate;      // 可空，后端用字典默认值兜底
    private BigDecimal qnetAr;
    private BigDecimal shortRate;
    private Integer payType;
    private Integer creditDays;
    private Integer transportBear;
}
