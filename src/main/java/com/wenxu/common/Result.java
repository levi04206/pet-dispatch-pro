package com.wenxu.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 全局统一返回结果类 (消除硬编码版)
 */
@Data
public class Result<T> implements Serializable {

    private Integer code;
    private String msg;
    private T data;

    // 内部私有构建方法，统一出口
    private static <T> Result<T> build(T data, Integer code, String msg) {
        Result<T> result = new Result<>();
        result.code = code;
        result.msg = msg;
        result.data = data;
        return result;
    }

    // --- 成功响应 ---
    public static <T> Result<T> success() {
        return build(null, ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg());
    }

    public static <T> Result<T> success(T data) {
        return build(data, ResultCodeEnum.SUCCESS.getCode(), ResultCodeEnum.SUCCESS.getMsg());
    }

    // --- 失败响应 ---
    // 基础失败（默认0）
    public static <T> Result<T> error(String msg) {
        return build(null, ResultCodeEnum.ERROR.getCode(), msg);
    }

    // 传递自定义状态码枚举
    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        return build(null, resultCodeEnum.getCode(), resultCodeEnum.getMsg());
    }
}