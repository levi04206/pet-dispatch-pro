package com.wenxu.controller;

import com.wenxu.converter.UserConverter;
import com.wenxu.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    void sendCodeShouldReturnSuccessWhenPhoneIsValid() throws Exception {
        when(userService.sendCode("13800138000")).thenReturn(true);

        mockMvc.perform(get("/api/user/sendCode").param("phone", "13800138000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("验证码发送成功"));
    }

    @Test
    void loginShouldReturnTokenWhenCodeIsValid() throws Exception {
        when(userService.login("13800138000", "123456")).thenReturn("token-001");

        mockMvc.perform(post("/api/user/login")
                        .param("phone", "13800138000")
                        .param("code", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("token-001"));
    }

    @Test
    void loginShouldReturnErrorWhenCodeIsInvalid() throws Exception {
        when(userService.login("13800138000", "000000")).thenReturn(null);

        mockMvc.perform(post("/api/user/login")
                        .param("phone", "13800138000")
                        .param("code", "000000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("验证码错误或已失效"));
    }
}
