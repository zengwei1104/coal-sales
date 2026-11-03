package com.nczy.coalsales.security;

import com.nczy.coalsales.entity.SysUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户封装：把业务实体 SysUser 包装成 Spring Security 认识的 UserDetails
 */
public class LoginUser implements UserDetails {

    private final SysUser user;

    /** 权限标识集合，如 "sys:user:add"；RBAC 未完成前可先传空 list */
    private final List<String> permissions;

    public LoginUser(SysUser user, List<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    /** 业务上常用：拿到当前登录用户的 id */
    public Long getUserId() {
        return user.getId();
    }

    /** 拿到角色 id（一对一） */
    public Long getRoleId() {
        return user.getRoleId();
    }

    /** 拿到完整实体 */
    public SysUser getUser() {
        return user;
    }

    /* ---------- UserDetails 接口实现 ---------- */

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return user.getStatus() != null && user.getStatus() == 1;
    }
}
