package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 回款率统计出参
 */
@Data
public class PaymentRateDTO {

    private BigDecimal receivable;   // 应收（已审核结算总额，元）

    private BigDecimal received;     // 实收（回款总额，元）

    private BigDecimal rate;         // 回款率（%），= received / receivable * 100
}
