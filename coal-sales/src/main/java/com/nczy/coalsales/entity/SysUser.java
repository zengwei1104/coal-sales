package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户表（对应 sys_user）
 */
@Data
@TableName("sys_user")
public class SysUser {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录名 */
    private String username;

    /** 密码（BCrypt 密文）—— 禁止序列化给前端 */
    @JsonIgnore
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 所属角色 id（一对一） */
    private Long roleId;

    /** 状态：1 启用 / 0 禁用 */
    private Integer status;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;

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
