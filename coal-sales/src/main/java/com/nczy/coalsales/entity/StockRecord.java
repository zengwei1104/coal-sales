package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存流水表（对应 stock_record）
 */
@Data
@TableName("stock_record")
public class StockRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 煤种商品 id */
    private Long productId;

    /** 来源类型：INBOUND 入库 / TICKET 磅单出库 / CHECK 盘点调整 */
    private String sourceType;

    /** 来源单据 id */
    private Long sourceId;

    /** 变动数量（吨），正=入库，负=出库 */
    private BigDecimal changeQty;

    /** 变动前库存 */
    private BigDecimal beforeQty;

    /** 变动后库存 */
    private BigDecimal afterQty;

    /** 备注 */
    private String remark;

    /** 操作人（用户 id） */
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;

    // ===== 以下为非表字段，仅用于列表返回 =====

    /** 煤种名称（来自 coal_product） */
    @TableField(exist = false)
    private String productName;
}
