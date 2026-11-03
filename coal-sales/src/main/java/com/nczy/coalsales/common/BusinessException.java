package com.nczy.coalsales.common;

import lombok.Getter;

/**
 * 业务异常
 * <p>
 * 用于业务规则校验失败时主动抛出，由 {@link GlobalExceptionHandler} 统一捕获并转换为响应体。
 * 默认错误码为 1001。
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误码，默认 1001 */
    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 1001;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
