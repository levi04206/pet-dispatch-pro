package com.wenxu.exception;

import com.wenxu.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

    @Test
    void handleIllegalArgumentShouldReturnBusinessMessage() {
        Result<String> result = globalExceptionHandler.handleIllegalArgument(new IllegalArgumentException("宠物不存在或无权下单"));

        assertEquals(0, result.getCode());
        assertEquals("宠物不存在或无权下单", result.getMsg());
    }

    @Test
    void handleExceptionShouldReturnGenericMessage() {
        Result<String> result = globalExceptionHandler.handleException(new RuntimeException("database password leaked"));

        assertEquals(0, result.getCode());
        assertEquals("系统繁忙，请稍后再试", result.getMsg());
    }
}
