package com.wenxu.aspect;

import com.wenxu.annotation.Idempotent;
import com.wenxu.common.BaseContext;
import com.wenxu.exception.RepeatSubmitException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotentAspectTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ProceedingJoinPoint joinPoint;

    private IdempotentAspect idempotentAspect;

    @BeforeEach
    void setUp() {
        idempotentAspect = new IdempotentAspect(stringRedisTemplate);
        BaseContext.setCurrentId(1001L);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/orders/create");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void aroundShouldProceedWhenFirstSubmit() throws Throwable {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("pet:idempotent:1001:/api/orders/create", "1", 5L, TimeUnit.SECONDS))
                .thenReturn(true);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = idempotentAspect.around(joinPoint, annotation());

        assertEquals("ok", result);
        verify(joinPoint).proceed();
    }

    @Test
    void aroundShouldThrowWhenSubmitRepeated() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("pet:idempotent:1001:/api/orders/create", "1", 5L, TimeUnit.SECONDS))
                .thenReturn(false);

        RepeatSubmitException exception = assertThrows(RepeatSubmitException.class,
                () -> idempotentAspect.around(joinPoint, annotation()));

        assertEquals("请求过于频繁，请稍后再试", exception.getMessage());
    }

    private Idempotent annotation() {
        try {
            Method method = DemoController.class.getDeclaredMethod("submit");
            return method.getAnnotation(Idempotent.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static class DemoController {

        @Idempotent
        public void submit() {
        }
    }
}
