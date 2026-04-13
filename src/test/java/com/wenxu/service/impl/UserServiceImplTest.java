package com.wenxu.service.impl;

import com.wenxu.constant.RedisConstants;
import com.wenxu.entity.User;
import com.wenxu.mapper.UserMapper;
import com.wenxu.utils.JwtUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void loginShouldRejectWrongCode() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisConstants.LOGIN_CODE_KEY + "13800138000")).thenReturn("123456");

        String token = userService.login("13800138000", "000000");

        assertNull(token);
        verify(userMapper, never()).selectOne(any());
        verify(jwtUtils, never()).createToken(any());
    }

    @Test
    void loginShouldReturnTokenForExistingUser() {
        User user = new User();
        user.setId(1L);
        user.setPhone("13800138000");

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisConstants.LOGIN_CODE_KEY + "13800138000")).thenReturn("123456");
        when(userMapper.selectOne(any())).thenReturn(user);
        when(jwtUtils.createToken(any())).thenReturn("token-001");

        String token = userService.login("13800138000", "123456");

        assertEquals("token-001", token);
        verify(stringRedisTemplate).delete(RedisConstants.LOGIN_CODE_KEY + "13800138000");
        verify(userMapper, never()).insert(any());
    }

    @Test
    void loginShouldRegisterNewUserWhenPhoneNotExists() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(RedisConstants.LOGIN_CODE_KEY + "13800138000")).thenReturn("123456");
        when(userMapper.selectOne(any())).thenReturn(null);
        when(jwtUtils.createToken(any())).thenReturn("token-002");

        String token = userService.login("13800138000", "123456");

        assertEquals("token-002", token);
        verify(userMapper).insert(any(User.class));
        verify(jwtUtils).createToken(any());
    }

    @Test
    void sendCodeShouldRejectInvalidPhone() {
        boolean sent = userService.sendCode("123");

        assertEquals(false, sent);
        verify(stringRedisTemplate, never()).opsForValue();
    }

    @Test
    void sendCodeShouldStoreCodeForValidPhone() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

        boolean sent = userService.sendCode("13800138000");

        assertEquals(true, sent);
        verify(valueOperations).set(eq(RedisConstants.LOGIN_CODE_KEY + "13800138000"), any(), eq(RedisConstants.LOGIN_CODE_TTL), eq(java.util.concurrent.TimeUnit.MINUTES));
    }
}
