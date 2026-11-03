package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表（对应 sys_log）
 */
@Data
@TableName("sys_log")
public class SysLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 操作用户 id */
    private Long userId;

    /** 操作用户名（冗余快照） */
    private String username;

    /** 操作内容（如「登录」「审核合同」） */
    private String operation;

    /** 请求方法（类名.方法名） */
    private String method;

    /** 请求参数（JSON） */
    private String params;

    /** 来源 IP */
    private String ip;

    /** 结果：1 成功 / 0 失败 */
    private Integer status;

    /** 耗时（毫秒） */
    private Integer costTime;

    /** 操作时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
