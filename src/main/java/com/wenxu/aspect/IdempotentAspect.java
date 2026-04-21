package com.wenxu.aspect;

import com.wenxu.annotation.Idempotent;
import com.wenxu.common.BaseContext;
import com.wenxu.constant.RedisConstants;
import com.wenxu.exception.RepeatSubmitException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class IdempotentAspect {

    private static final String REPEAT_SUBMIT_MESSAGE = "请求过于频繁，请稍后再试";

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        HttpServletRequest request = currentRequest();
        Long currentUserId = BaseContext.getCurrentId();
        String userPart = currentUserId == null ? "anonymous" : String.valueOf(currentUserId);
        String redisKey = RedisConstants.IDEMPOTENT_KEY_PREFIX + userPart + ":" + request.getRequestURI();
        Boolean success = stringRedisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", idempotent.expireTime(), TimeUnit.SECONDS);
        if (!Boolean.TRUE.equals(success)) {
            throw new RepeatSubmitException(REPEAT_SUBMIT_MESSAGE);
        }
        return joinPoint.proceed();
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new IllegalStateException("当前请求上下文不存在");
        }
        return Objects.requireNonNull(servletRequestAttributes.getRequest());
    }
}
