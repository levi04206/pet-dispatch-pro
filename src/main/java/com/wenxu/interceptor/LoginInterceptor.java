package com.wenxu.interceptor;

import com.wenxu.common.BaseContext;
import com.wenxu.common.UserRoleEnum;
import com.wenxu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        try {
            // 解析 token 后，把当前用户身份写入线程上下文，Controller 和 Service 通过 BaseContext 获取。
            Claims claims = jwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            Object roleClaim = claims.get("role");
            String role = roleClaim == null ? UserRoleEnum.USER.name() : roleClaim.toString();

            BaseContext.setCurrentId(userId);
            BaseContext.setCurrentRole(role);

            return true;
        } catch (Exception e) {
            // token 无效时清理上下文，避免线程复用导致旧身份残留。
            BaseContext.removeCurrentId();
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理 ThreadLocal，防止当前用户信息影响下一次请求。
        BaseContext.removeCurrentId();
    }
}
