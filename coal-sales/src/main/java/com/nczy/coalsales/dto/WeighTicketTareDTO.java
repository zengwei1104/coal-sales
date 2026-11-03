package com.nczy.coalsales.dto;

import com.nczy.coalsales.entity.SaleOrder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 磅单开单 / 称皮重入参
 */
@Data
public class WeighTicketTareDTO {
    private Long orderId;          // 必填：挂哪个订单
    private String plateNo;        // 必填：车牌号
    private String driver;         // 司机（可选）
    private Integer tareSource;    // 皮重来源：1 现场称重 / 2 备案套用
    private BigDecimal tareWeight; // 皮重（吨），现场称重时必填


}
