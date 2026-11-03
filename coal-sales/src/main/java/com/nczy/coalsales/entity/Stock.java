package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存表（对应 stock）
 */
@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 煤种商品 id */
    private Long productId;

    /** 当前库存（吨） */
    private BigDecimal totalQty;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    // ===== 以下为非表字段，仅用于列表返回 =====

    /** 煤种名称（来自 coal_product） */
    @TableField(exist = false)
    private String productName;

    /** 安全库存（来自 coal_product） */
    @TableField(exist = false)
    private BigDecimal safeStock;

    /** 是否预警：当前库存 < 安全库存 */
    @TableField(exist = false)
    private Boolean warning;
}
