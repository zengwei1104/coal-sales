package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同表（对应 contract）
 */
@Data
@TableName("contract")
public class Contract {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String contractNo;      // 合同编号
    private Long customerId;        // 客户 id
    private Long productId;         // 煤种商品 id
    private LocalDate signDate;     // 签订日期
    private LocalDate startDate;    // 有效期起
    private LocalDate endDate;      // 有效期止
    private BigDecimal totalQty;    // 约定总量（吨）
    private BigDecimal price;       // 含税单价
    private BigDecimal taxRate;     // 税率
    private BigDecimal qnetAr;      // 约定热值
    private BigDecimal shortRate;   // 溢短装比例
    private Integer payType;        // 付款方式
    private Integer creditDays;     // 账期天数
    private Integer transportBear;  // 运费承担
    private Integer status;         // 状态

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;          // 创建人

    private Long auditBy;           // 审核人
    private LocalDateTime auditTime; // 审核时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
