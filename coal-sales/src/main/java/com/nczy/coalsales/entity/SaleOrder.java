package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 销售订单表（对应 sale_order）
 */
@Data
@TableName("sale_order")
public class SaleOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;        // 订单编号（DD+年月日+流水）
    private Long contractId;       // 合同 id
    private Long customerId;       // 客户 id（冗余快照）
    private Long productId;        // 煤种商品 id（从合同带出）
    private BigDecimal planQty;    // 计划数量（吨）
    private BigDecimal shippedQty; // 已发运量（吨）
    private BigDecimal settledQty; // 已结算量（吨）
    private BigDecimal price;      // 价格快照（元/吨）
    private String priceRemark;    // 手动改价的理由
    private LocalDate deliveryDate;// 要求发货日期
    private String plateNo;        // 指定车牌
    private Integer status;        // 状态：1 待发运 / 2 部分发运 / 3 已完成 / 4 已取消

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;         // 创建人

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
