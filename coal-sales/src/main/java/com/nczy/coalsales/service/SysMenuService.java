package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.entity.SysMenu;

import java.util.List;

/**
 * 菜单服务
 */
public interface SysMenuService extends IService<SysMenu> {

    /**
     * 查询完整菜单树（目录下挂子菜单），供菜单管理页面使用
     */
    List<SysMenu> tree();
}
