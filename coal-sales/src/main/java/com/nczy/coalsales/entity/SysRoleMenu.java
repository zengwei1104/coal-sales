package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 角色菜单关联表（对应 sys_role_menu）
 */
@Data
@TableName("sys_role_menu")
public class SysRoleMenu {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色 id */
    private Long roleId;

    /** 菜单 id */
    private Long menuId;
}
