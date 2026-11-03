package com.nczy.coalsales.service;

import com.nczy.coalsales.annotation.Log;
import com.nczy.coalsales.common.BusinessException;
import com.nczy.coalsales.dto.LoginRequest;
import com.nczy.coalsales.dto.LoginResponse;
import com.nczy.coalsales.dto.LoginUserInfo;
import com.nczy.coalsales.entity.SysRole;
import com.nczy.coalsales.entity.SysUser;
import com.nczy.coalsales.mapper.SysMenuMapper;
import com.nczy.coalsales.mapper.SysRoleMapper;
import com.nczy.coalsales.mapper.SysUserMapper;
import com.nczy.coalsales.security.JwtUtil;
import com.nczy.coalsales.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 认证服务：登录校验、签发 token、组装用户信息
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysDictService sysDictService;

    @Log("登录")
    public LoginResponse login(LoginRequest request) {
        // 1. 用 Spring Security 的 AuthenticationManager 做认证（内部会调 UserDetailsServiceImpl + BCrypt 比对）
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (Exception e) {
            // 认证失败统一抛出业务异常，避免把 Security 内部异常暴露给前端
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 从认证结果拿到 LoginUser（含完整 SysUser 实体）
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        SysUser user = loginUser.getUser();

        // 3. 查角色编码
        String roleCode = null;
        if (user.getRoleId() != null) {
            SysRole role = sysRoleMapper.selectById(user.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
            }
        }

        // 4. 查该角色可访问的菜单路由
        List<String> menus = sysMenuMapper.selectMenuPathsByRoleId(user.getRoleId());


        // 5. 更新最后登录时间
        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(update);

        int expireHours = sysDictService.getParamAsInt("jwt_expire_hours", 2);
        // 6. 签发 token
        String token = JwtUtil.generateToken(user.getUsername(), user.getId(),expireHours);

        LoginUserInfo userInfo = LoginUserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .roleCode(roleCode)
                .menus(menus)
                .build();

        return LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }
}
