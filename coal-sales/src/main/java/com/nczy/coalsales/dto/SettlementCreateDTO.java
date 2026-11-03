package com.nczy.coalsales.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 生成结算单入参
 */
@Data
public class SettlementCreateDTO {

    @NotNull(message = "合同不能为空")
    private Long contractId;

    @NotEmpty(message = "请至少选择一张磅单")
    private List<Long> ticketIds;
}
