package com.nczy.coalsales.service;

import com.baomidou.mybatisplus.spring.service.IService;
import com.nczy.coalsales.entity.SysRoleMenu;

import java.util.List;

/**
 * 角色菜单关联服务
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 给角色分配菜单（全量重设）：先删旧关系，再批量插入新关系。
     * menuIds 为空时只删不插（即清空该角色菜单）。
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 查某角色已分配的菜单 id 列表（回显勾选用）
     */
    List<Long> listMenuIdsByRole(Long roleId);
}
