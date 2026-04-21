package com.wenxu.aspect;

import com.wenxu.annotation.LogOperation;
import com.wenxu.common.BaseContext;
import com.wenxu.entity.OperationLog;
import com.wenxu.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogOperationAspect {

    private final OperationLogService operationLogService;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long start = System.currentTimeMillis();
        Throwable error = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            error = ex;
            throw ex;
        } finally {
            saveOperationLog(logOperation, start, error);
        }
    }

    private void saveOperationLog(LogOperation logOperation, long start, Throwable error) {
        try {
            HttpServletRequest request = currentRequest();
            OperationLog operationLog = new OperationLog();
            operationLog.setUserId(BaseContext.getCurrentId());
            operationLog.setRole(BaseContext.getCurrentRole());
            operationLog.setModule(logOperation.module());
            operationLog.setAction(buildAction(logOperation.action(), error));
            operationLog.setRequestPath(request.getMethod() + " " + request.getRequestURI());
            operationLog.setIp(resolveClientIp(request));
            operationLog.setCostTimeMs(System.currentTimeMillis() - start);
            operationLog.setCreateTime(LocalDateTime.now());
            operationLogService.saveAsync(operationLog);
        } catch (Exception ex) {
            log.warn("Save operation log skipped: {}", ex.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            throw new IllegalStateException("当前请求上下文不存在");
        }
        return Objects.requireNonNull(servletRequestAttributes.getRequest());
    }

    private String buildAction(String action, Throwable error) {
        if (error == null) {
            return action;
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return action + " [FAILED]";
        }
        return action + " [FAILED] " + message;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
