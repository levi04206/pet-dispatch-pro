package com.wenxu.interceptor;

import com.wenxu.common.BaseContext;
import com.wenxu.common.UserRoleEnum;
import com.wenxu.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginInterceptorTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Claims claims;

    @InjectMocks
    private LoginInterceptor loginInterceptor;

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void preHandleShouldStoreUserIdAndRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "token-001");
        when(jwtUtils.parseToken("token-001")).thenReturn(claims);
        when(claims.get("userId")).thenReturn(100L);
        when(claims.get("role")).thenReturn(UserRoleEnum.ADMIN.name());

        boolean allowed = loginInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(allowed);
        assertEquals(100L, BaseContext.getCurrentId());
        assertEquals(UserRoleEnum.ADMIN.name(), BaseContext.getCurrentRole());
    }

    @Test
    void preHandleShouldFallbackToUserRoleWhenTokenHasNoRole() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "token-001");
        when(jwtUtils.parseToken("token-001")).thenReturn(claims);
        when(claims.get("userId")).thenReturn(100L);
        when(claims.get("role")).thenReturn(null);

        boolean allowed = loginInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertTrue(allowed);
        assertEquals(UserRoleEnum.USER.name(), BaseContext.getCurrentRole());
    }

    @Test
    void preHandleShouldRejectInvalidToken() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("token", "bad-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtUtils.parseToken("bad-token")).thenThrow(new RuntimeException("bad token"));

        boolean allowed = loginInterceptor.preHandle(request, response, new Object());

        assertFalse(allowed);
        assertEquals(401, response.getStatus());
    }
}
