package com.nczy.coalsales.controller;

import com.nczy.coalsales.common.Result;
import com.nczy.coalsales.dto.LoginRequest;
import com.nczy.coalsales.dto.LoginResponse;
import com.nczy.coalsales.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }
    // AuthController 里加一个方法
    @PostMapping("/logout")
    public Result<Void> logout() {
        // JWT 无状态，服务端无需处理，前端删除本地 token 即可
        // 后续可在此记录退出日志
        return Result.success("退出成功", null);
    }
}
