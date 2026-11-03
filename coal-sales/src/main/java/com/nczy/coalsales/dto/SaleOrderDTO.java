package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SaleOrderDTO {

    @NotNull(message = "合同不能为空")
    private Long contractId;

    @NotNull(message = "计划数量不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "计划数量必须大于0")
    private BigDecimal planQty;

    private BigDecimal price;        // 可空，不传取合同价
    private String priceRemark;      // 改价理由
    private LocalDate deliveryDate;  // 要求发货日期
    private String plateNo;          // 指定车牌
}
