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

    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    @Override
    public boolean sendCode(String phone) {
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
        if (cacheCode == null || !cacheCode.equals(code)) {
            return null;
        }
        stringRedisTemplate.delete(redisKey);

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            user = registerUserWithPhone(phone);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());
        claims.put("role", resolveRole(user.getRole()));
        return jwtUtils.createToken(claims);
    }

    @Override
    public User testInsertAndQuery() {
        User user = new User();
        user.setPhone("13800138002");
        user.setNickname("测试铲屎官");

        userMapper.insert(user);
        return userMapper.selectById(user.getId());
    }

    private User registerUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickname("铲屎官_" + RandomUtil.randomNumbers(6));
        user.setStatus(1);
        user.setRole(UserRoleEnum.USER.name());
        userMapper.insert(user);
        return user;
    }

    private String resolveRole(String role) {
        return role == null || role.isBlank() ? UserRoleEnum.USER.name() : role;
    }
}
