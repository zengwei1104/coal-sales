package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算明细表（对应 settlement_item）
 */
@Data
@TableName("settlement_item")
public class SettlementItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long settleId;          // 结算单 id

    private Long ticketId;          // 磅单 id（一张磅单最多被结算一次）

    private Long productId;         // 煤种商品 id

    private BigDecimal netWeight;   // 净重（吨），从磅单抄来

    private BigDecimal price;       // 单价（元/吨），默认合同价

    private BigDecimal amount;      // 金额（元）= 净重 × 单价

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
