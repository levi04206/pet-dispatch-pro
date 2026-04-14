package com.wenxu.interceptor;

import com.wenxu.common.BaseContext;
import com.wenxu.common.UserRoleEnum;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminInterceptorTest {

    private final AdminInterceptor adminInterceptor = new AdminInterceptor();

    @AfterEach
    void tearDown() {
        BaseContext.removeCurrentId();
    }

    @Test
    void preHandleShouldAllowAdminRole() {
        BaseContext.setCurrentRole(UserRoleEnum.ADMIN.name());

        boolean allowed = adminInterceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), new Object());

        assertTrue(allowed);
    }

    @Test
    void preHandleShouldRejectNonAdminRole() {
        BaseContext.setCurrentRole(UserRoleEnum.USER.name());
        MockHttpServletResponse response = new MockHttpServletResponse();

        boolean allowed = adminInterceptor.preHandle(new MockHttpServletRequest(), response, new Object());

        assertFalse(allowed);
        assertEquals(403, response.getStatus());
    }
}
