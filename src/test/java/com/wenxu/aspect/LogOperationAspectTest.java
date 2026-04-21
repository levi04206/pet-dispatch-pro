package com.wenxu.aspect;

import com.wenxu.annotation.LogOperation;
import com.wenxu.common.BaseContext;
import com.wenxu.entity.OperationLog;
import com.wenxu.service.OperationLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogOperationAspectTest {

    @Mock
    private OperationLogService operationLogService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private LogOperationAspect logOperationAspect;

    @BeforeEach
    void setUp() {
        logOperationAspect = new LogOperationAspect(operationLogService);
        BaseContext.setCurrentId(2002L);
        BaseContext.setCurrentRole("SITTER");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/orders/reject");
        request.addHeader("X-Forwarded-For", "127.0.0.1, 10.0.0.1");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void aroundShouldSaveOperationLogAsync() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = logOperationAspect.around(joinPoint, annotation());

        assertEquals("ok", result);
        ArgumentCaptor<OperationLog> captor = ArgumentCaptor.forClass(OperationLog.class);
        verify(operationLogService).saveAsync(captor.capture());
        OperationLog operationLog = captor.getValue();
        assertEquals(2002L, operationLog.getUserId());
        assertEquals("SITTER", operationLog.getRole());
        assertEquals("订单模块", operationLog.getModule());
        assertEquals("宠托师拒单", operationLog.getAction());
        assertEquals("POST /api/orders/reject", operationLog.getRequestPath());
        assertEquals("127.0.0.1", operationLog.getIp());
        assertTrue(operationLog.getCostTimeMs() >= 0L);
    }

    private LogOperation annotation() {
        try {
            Method method = DemoController.class.getDeclaredMethod("reject");
            return method.getAnnotation(LogOperation.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class DemoController {

        @LogOperation(module = "订单模块", action = "宠托师拒单")
        public void reject() {
        }
    }
}
