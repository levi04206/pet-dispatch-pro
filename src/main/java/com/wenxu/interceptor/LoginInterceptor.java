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
            Claims claims = jwtUtils.parseToken(token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            Object roleClaim = claims.get("role");
            String role = roleClaim == null ? UserRoleEnum.USER.name() : roleClaim.toString();

            BaseContext.setCurrentId(userId);
            BaseContext.setCurrentRole(role);

            return true;
        } catch (Exception e) {
            BaseContext.removeCurrentId();
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        BaseContext.removeCurrentId();
    }
}
