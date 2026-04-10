package com.wenxu.interceptor;

import com.wenxu.common.BaseContext;
import com.wenxu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        try {
            Claims claims = JwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());

            // 🚨 核心改动：把 userId 存入当前线程的口袋！
            BaseContext.setCurrentId(userId);

            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 🚨 请求结束后，清理口袋，大厂规范！
        BaseContext.removeCurrentId();
    }
}