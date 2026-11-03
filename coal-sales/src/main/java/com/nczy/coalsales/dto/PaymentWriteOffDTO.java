package com.nczy.coalsales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 回款核销入参
 */
@Data
public class PaymentWriteOffDTO {

    @NotEmpty(message = "核销明细不能为空")
    private List<@Valid WriteOffItemDTO> items;
}
