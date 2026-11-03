package com.nczy.coalsales.controller;

import com.nczy.coalsales.Util.AuthUtil;
import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.constant.RoleConstant;
import com.nczy.coalsales.dto.RoleMenuDTO;
import com.nczy.coalsales.entity.SysRole;
import com.nczy.coalsales.security.LoginUser;
import com.nczy.coalsales.service.SysRoleMenuService;
import com.nczy.coalsales.service.SysRoleService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final SysRoleService sysRoleService;
    private final SysRoleMenuService sysRoleMenuService;

    public RoleController(SysRoleService sysRoleService,
                          SysRoleMenuService sysRoleMenuService) {
        this.sysRoleService = sysRoleService;
        this.sysRoleMenuService = sysRoleMenuService;
    }

    @GetMapping
    public Result<List<SysRole>> getRoles() {
        return Result.success(sysRoleService.getRoles());
    }

    /**
     * 给角色分配菜单（全量重设，仅管理员）
     */
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable("id") Long id,
                                    @RequestBody RoleMenuDTO dto,
                                    @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        sysRoleMenuService.assignMenus(id, dto.getMenuIds());
        return Result.success("分配成功", null);
    }

    /**
     * 查角色已分配的菜单 id（回显勾选，仅管理员）
     */
    @GetMapping("/{id}/menus")
    public Result<List<Long>> listMenuIds(@PathVariable("id") Long id,
                                          @AuthenticationPrincipal LoginUser loginUser) {
        AuthUtil.checkRole(loginUser, RoleConstant.ADMIN);
        return Result.success(sysRoleMenuService.listMenuIdsByRole(id));
    }
}
