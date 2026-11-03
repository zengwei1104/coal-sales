package com.nczy.coalsales.common;

import lombok.Data;

/**
 * 统一响应体
 * <p>
 * 约定：code = 200 表示成功，非 200 表示失败（具体错误码见接口设计文档第 1.7 节）。
 * <pre>
 * {
 *   "code": 200,
 *   "message": "操作成功",
 *   "data": { ... }
 * }
 * </pre>
 */
@Data
public class Result<T> {

    /** 状态码：200 成功，其余为业务/系统错误 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /* ---------------- 成功 ---------------- */

    public static <T> Result<T> success() {
        return build(200, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return build(200, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return build(200, message, data);
    }

    /* ---------------- 失败 ---------------- */

    public static <T> Result<T> error(String message) {
        return build(1001, message, null);
    }

    public static <T> Result<T> error(Integer code, String message) {
        return build(code, message, null);
    }

    /* ---------------- 工具方法 ---------------- */

    private static <T> Result<T> build(Integer code, String message, T data) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.data = data;
        return result;
    }
}
