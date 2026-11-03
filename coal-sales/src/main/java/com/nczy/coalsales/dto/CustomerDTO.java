package com.nczy.coalsales.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class CustomerDTO {
    private Long id;

    @NotBlank(message = "客户名称不能为空")
    private String customerName;

    @NotBlank(message = "联系人不能为空")
    private String contactName;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String address;
    private String invoiceInfo;

    @DecimalMin(value = "0", message = "信用额度不能为负数")
    private BigDecimal creditLimit;

    private String region;
    private String remark;
}



