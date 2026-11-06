package com.nczy.coalsales.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRankDTO {
    private  Long customerId;
    private  String customerName;
    private BigDecimal Amount;
}
