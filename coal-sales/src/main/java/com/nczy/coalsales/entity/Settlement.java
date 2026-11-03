package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算单表（对应 settlement）
 */
@Data
@TableName("settlement")
public class Settlement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String settleNo;        // 结算单号（JS+年月日+流水）

    private Long contractId;        // 合同 id（结算单挂合同，不挂订单）

    private Long customerId;        // 客户 id（冗余快照）

    private BigDecimal totalQty;    // 结算总净重（吨）

    private BigDecimal totalAmount; // 结算总额（元，含税）= Σ明细金额

    private BigDecimal taxAmount;   // 税额（元）

    private Integer status;         // 状态：0 待审核 / 1 已审核 / 2 已作废

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;          // 创建人（销售员）

    private Long auditBy;           // 审核人（财务）

    private LocalDateTime auditTime; // 审核时间

    private String remark;          // 备注

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;        // 逻辑删除

    // ===== 以下为非表字段，仅用于「可核销结算单」查询时回传汇总值 =====
    @TableField(exist = false)
    private BigDecimal receivedAmount;   // 已核销金额（从 write_off 汇总，实时）

    @TableField(exist = false)
    private BigDecimal remainingAmount;  // 剩余未收 = totalAmount - receivedAmount
}
