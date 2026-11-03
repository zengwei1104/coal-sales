package com.nczy.coalsales.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 结算单审核入参
 */
@Data
public class SettlementAuditDTO {

    @NotNull(message = "审核结果不能为空")
    private Boolean pass;

    private String opinion;
}
