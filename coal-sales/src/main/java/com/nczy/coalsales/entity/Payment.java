package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回款表（对应 payment）
 */
@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String paymentNo;       // 回款单号（HK+年月日+流水）

    private Long customerId;        // 客户 id

    private BigDecimal amount;      // 回款金额（元）

    private Integer payType;        // 付款方式：1 银行转账 / 2 现金 / 3 承兑汇票

    private LocalDateTime payTime;  // 付款时间

    private String remark;          // 备注（银行回单号等）

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;          // 登记人（财务）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
