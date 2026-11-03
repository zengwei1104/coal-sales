package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 回款核销明细表（对应 write_off）
 */
@Data
@TableName("write_off")
public class WriteOff {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long paymentId;         // 回款 id

    private Long settleId;          // 结算单 id

    private BigDecimal amount;      // 本次冲抵金额（元）

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
