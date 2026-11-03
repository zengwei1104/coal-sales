package com.nczy.coalsales.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 登录响应体：token + 用户信息
 */
@Data
@Builder
public class LoginResponse {

    private String token;

    private LoginUserInfo userInfo;
}
