package com.wenxu.service.impl;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wenxu.common.UserRoleEnum;
import com.wenxu.constant.RedisConstants;
import com.wenxu.entity.User;
import com.wenxu.mapper.UserMapper;
import com.wenxu.service.UserService;
import com.wenxu.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    private static final int USER_STATUS_NORMAL = 1;

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean sendCode(String phone) {
        // 发送验证码前先校验手机号格式，避免无效手机号写入 Redis。
        if (!PhoneUtil.isMobile(phone)) {
            return false;
        }

        String code = RandomUtil.randomNumbers(6);
        String redisKey = RedisConstants.LOGIN_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(redisKey, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        System.out.println("=======================================================");
        System.out.println("[PetDispatch Pro] 向手机号 " + phone + " 发送短信成功");
        System.out.println("[PetDispatch Pro] 您的登录验证码为：" + code + "，5 分钟内有效");
        System.out.println("=======================================================");
        return true;
    }

    @Override
    public String login(String phone, String code) {
        String redisKey = RedisConstants.LOGIN_CODE_KEY + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(redisKey);
        // 验证码不存在或不匹配时直接登录失败。
        if (cacheCode == null || !cacheCode.equals(code)) {
            return null;
        }
        stringRedisTemplate.delete(redisKey);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            user = registerUserWithPhone(phone);
        }
        // 封禁用户即使验证码正确，也不能获取登录态。
        if (!Integer.valueOf(USER_STATUS_NORMAL).equals(user.getStatus())) {
            return null;
        }

        // JWT 中写入用户 ID、手机号和角色，后续拦截器从 token 中恢复当前登录身份。
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());
        claims.put("role", resolveRole(user.getRole()));
        return jwtUtils.createToken(claims);
    }

    private User registerUserWithPhone(String phone) {
        // 首次验证码登录时自动注册普通用户。
        User user = new User();
        user.setPhone(phone);
        user.setNickname("铲屎官_" + RandomUtil.randomNumbers(6));
        user.setStatus(USER_STATUS_NORMAL);
        user.setRole(UserRoleEnum.USER.name());
        userMapper.insert(user);
        return user;
    }

    private String resolveRole(String role) {
        return role == null || role.isBlank() ? UserRoleEnum.USER.name() : role;
    }
}
