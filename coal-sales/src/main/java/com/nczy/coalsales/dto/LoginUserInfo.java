package com.nczy.coalsales.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 登录成功返回的用户信息
 */
@Data
@Builder
public class LoginUserInfo {

    /** 用户 id */
    private Long id;

    /** 登录名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 角色编码（ADMIN/MANAGER/SALES/WEIGHMAN/STOCK/FINANCE） */
    private String roleCode;

    /** 可访问的路由路径列表，前端据此渲染侧边栏 */
    private List<String> menus;
}
