package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客户表（对应 customer）
 */
@Data
@TableName("customer")
public class Customer {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 客户单位名称 */
    private String customerName;

    /** 联系人 */
    private String contactName;

    /** 联系电话 */
    private String phone;

    /** 收货地址 */
    private String address;

    /** 开票信息（税号/开户行等） */
    private String invoiceInfo;

    /** 信用额度（元） */
    private BigDecimal creditLimit;

    /** 地区 */
    private String region;

    /** 备注 */
    private String remark;

    /** 创建人（用户 id） */
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;

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
