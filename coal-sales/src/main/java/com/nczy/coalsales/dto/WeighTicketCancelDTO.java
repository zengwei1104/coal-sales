package com.nczy.coalsales.dto;

import lombok.Data;

/**
 * 磅单作废入参
 */
@Data
public class WeighTicketCancelDTO {
    private String cancelReason; // 作废原因（必填）
}
