package com.wenxu.common;

/**
 * 全局统一状态码枚举
 */
public enum ResultCodeEnum {

    // --- 成功状态 ---
    SUCCESS(1, "操作成功"),

    // --- 失败状态 ---
    ERROR(0, "操作失败"),
    PARAM_ERROR(400, "参数校验错误"),
    UNAUTHORIZED(401, "暂未登录或Token已过期"),
    FORBIDDEN(403, "没有相关权限"),
    NOT_FOUND(404, "资源不存在"),
    SYSTEM_ERROR(500, "系统内部异常，请联系管理员");

    private final Integer code;
    private final String msg;

    ResultCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}