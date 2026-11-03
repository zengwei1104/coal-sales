package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 磅单（汽车衡计量单）
 */
@Data
@TableName("weigh_ticket")
public class WeighTicket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String weightNo;      // 磅单号（BD+年月日+流水）

    private Long orderId;         // 订单 id

    private Long productId;       // 煤种商品 id（从订单带出）

    private String plateNo;       // 车牌号

    private String driver;        // 司机

    private BigDecimal tareWeight; // 皮重（吨）

    private LocalDateTime tareTime; // 空车过磅时间

    private Integer tareSource;   // 皮重来源：1 现场称重 / 2 备案套用

    private BigDecimal grossWeight; // 毛重（吨）

    private LocalDateTime grossTime; // 重车过磅时间

    private BigDecimal netWeight;  // 净重（吨）= 毛重 − 皮重

    private Integer status;       // 状态：0 待称皮重 / 1 已称皮重 / 2 已完成 / 3 已作废

    private Long weighmanId;      // 司磅员（用户 id）

    private String cancelReason;  // 作废原因
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;      // 逻辑删除

    // ===== getter / setter =====
    // ... 省略，需要我生成完整版可以切 Agent 模式
}
