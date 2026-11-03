package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 煤种商品表（对应 coal_product）
 */
@Data
@TableName("coal_product")
public class CoalProduct {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 煤种/商品名称（如无烟煤、烟煤） */
    private String productName;

    /** 规格（如 3-6 块、粉煤） */
    private String spec;

    /** 收到基低位发热量（kcal/kg） */
    private BigDecimal qnetAr;

    /** 硫分（%） */
    private BigDecimal sulfur;

    /** 灰分（%） */
    private BigDecimal ash;

    /** 产地 */
    private String origin;

    /** 基准价（元/吨） */
    private BigDecimal basePrice;

    /** 安全库存（吨），触发预警线 */
    private BigDecimal safeStock;

    /** 状态：1 上架 / 0 下架 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 逻辑删除：0 正常 / 1 已删 */
    @TableLogic
    private Integer deleted;
}
