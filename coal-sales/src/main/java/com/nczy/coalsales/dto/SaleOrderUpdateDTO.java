package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaleOrderUpdateDTO {

    @DecimalMin(value = "0", inclusive = false, message = "计划数量必须大于0")
    private BigDecimal planQty;

    private BigDecimal price;
    private String priceRemark;
    private LocalDate deliveryDate;
    private String plateNo;
}
