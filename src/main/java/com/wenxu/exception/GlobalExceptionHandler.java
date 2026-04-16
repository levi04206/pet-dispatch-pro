package com.wenxu.exception;

import com.wenxu.common.Result;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "系统繁忙，请稍后再试";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        // JSON 请求体参数校验失败时，返回第一个字段错误，方便前端直接展示。
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse("Request parameter validation failed");
        return Result.error(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Result<String> handleConstraintViolation(ConstraintViolationException ex) {
        // Query 参数或 Path 参数校验失败时，返回第一个约束错误。
        String message = ex.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse("Request parameter validation failed");
        return Result.error(message);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<String> handleIllegalArgument(IllegalArgumentException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public Result<String> handleIllegalState(IllegalStateException ex) {
        return Result.error(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception ex) {
        // 未预期异常只记录日志，响应给前端统一兜底文案，避免泄露内部细节。
        log.error("Unhandled exception", ex);
        return Result.error(INTERNAL_ERROR_MESSAGE);
    }
}
