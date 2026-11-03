package com.nczy.coalsales.Util;

import com.nczy.coalsales.security.LoginUser;
import org.springframework.security.access.AccessDeniedException;

/**
 * 权限校验工具类
 */
public class AuthUtil {

    /** 校验当前用户是否属于允许的角色 */
    public static void checkRole(LoginUser loginUser, Long... allowedRoles) {
        if (loginUser == null || loginUser.getRoleId() == null) {
            throw new AccessDeniedException("未登录或没有操作权限");
        }
        for (Long role : allowedRoles) {
            if (role.equals(loginUser.getRoleId())) {
                return; // 命中允许角色，放行
            }
        }
        throw new AccessDeniedException("没有操作权限");
    }
}
