package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 磅单称毛重入参
 */
@Data
public class WeighTicketGrossDTO {
    private BigDecimal grossWeight; // 毛重（吨），必填
}
