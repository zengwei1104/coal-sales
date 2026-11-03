package com.nczy.coalsales.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 菜单表（对应 sys_menu）
 */
@Data
@TableName("sys_menu")
public class SysMenu {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 父菜单 id，0 表示顶级 */
    private Long parentId;

    /** 菜单名称 */
    private String menuName;

    /** 类型：1 目录 / 2 菜单 */
    private Integer menuType;

    /** 前端路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 图标 */
    private String icon;

    /** 排序号 */
    private Integer sortOrder;

    /** 状态：1 启用 / 0 禁用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除：0 正常 / 1 已删 */
    @TableLogic
    private Integer deleted;

    /** 子菜单（树形组装用，非数据库字段） */
    @TableField(exist = false)
    private List<SysMenu> children;
}
