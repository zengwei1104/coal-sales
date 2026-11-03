package com.nczy.coalsales.security;

import com.nczy.coalsales.entity.SysUser;
import com.nczy.coalsales.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final SysUserMapper sysUserMapper;

    // 构造器注入（Lombok 或手写都行，下面用手写，和项目现有风格一致）
    public JwtAuthenticationFilter(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            try {
                Claims claims = JwtUtil.parseToken(token);

                // 从 token 里取 userId（生成 token 时用 .claim("userId", userId) 存过）
                Long userId = claims.get("userId", Long.class);

                // 回查数据库，拿到最新用户状态（同时能校验是否被禁用）
                SysUser user = sysUserMapper.selectById(userId);

                if (user != null && user.getStatus() != null && user.getStatus() == 1) {
                    // 用真实用户包装成 LoginUser（权限列表先传空，后续做按钮级权限再补）
                    LoginUser loginUser = new LoginUser(user, Collections.emptyList());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // 用户不存在或已禁用 → 不放行
                    SecurityContextHolder.clearContext();
                }
            } catch (Exception e) {
                // token 解析失败 / 用户不存在 → 清空上下文
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
