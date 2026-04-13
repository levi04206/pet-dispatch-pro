package com.wenxu.controller;

import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.RandomUtil;
import com.wenxu.common.Result;
import com.wenxu.common.ResultCodeEnum;
import com.wenxu.constant.RedisConstants;
import com.wenxu.entity.User;
import com.wenxu.mapper.UserMapper;
import com.wenxu.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource // 把刚才的 Mapper 引擎注入进来
    private UserMapper userMapper;

    // 🚨新增：注入 Redis 操作模板
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 发送手机验证码并存入 Redis
     */
    @GetMapping("/sendCode")
    public Result<String> sendCode(@RequestParam String phone) {

        // 1. 校验手机号是否合法 (利用 Hutool 神器，免去手写恶心的正则表达式)
        if (!PhoneUtil.isMobile(phone)) {
            // 这里用上了咱们刚重构的枚举类！优雅！
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }

        // 2. 生成 6 位纯数字随机验证码
        String code = RandomUtil.randomNumbers(6);

        // 3. 拼接 Redis 的 Key
        // 大厂规范：业务名:模块名:唯一标识 (利用冒号，在 Redis 可视化工具里会自动分层级变成文件夹)
        String redisKey = RedisConstants.LOGIN_CODE_KEY + phone;

        // 4. 存入 Redis，并强制设置 5 分钟过期时间 (TTL)
        stringRedisTemplate.opsForValue().set(redisKey, code, RedisConstants.LOGIN_CODE_TTL, TimeUnit.MINUTES);

        // 5. 模拟发送短信 (真实环境这里会调用阿里云/腾讯云的短信 API)
        // 咱们在控制台打印出来，假装手机收到了短信
        System.out.println("=======================================================");
        System.out.println("【PetDispatch Pro】向手机号 " + phone + " 发送短信成功！");
        System.out.println("【PetDispatch Pro】您的登录验证码为：" + code + "，5分钟内有效！");
        System.out.println("=======================================================");

        return Result.success("验证码发送成功");
    }

    /**
     * 第二战：登录与自动注册
     * @param phone 手机号
     * @param code  用户输入的验证码
     */
    @PostMapping("/login")
    public Result<String> login(@RequestParam String phone, @RequestParam String code) {
        // 1. 验证码校验逻辑 (保持不变...)
        String redisKey = RedisConstants.LOGIN_CODE_KEY + phone;
        String cacheCode = stringRedisTemplate.opsForValue().get(redisKey);
        if (cacheCode == null || !cacheCode.equals(code)) {
            return Result.error("验证码错误或已失效");
        }
        stringRedisTemplate.delete(redisKey);

        // 2. 查询/自动注册逻辑 (保持不变...)
        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getPhone, phone));
        if (user == null) {
            user = registerUserWithPhone(phone);
        }

        // 3. --- 核心升级：制作电子身份证 (JWT) ---
        // 准备载荷 (Payload)：把用户的核心 ID 塞进去，这样以后我们解析 Token 就能知道是谁在操作
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getId());
        claims.put("phone", user.getPhone());

        // 生成令牌
        String token = jwtUtils.createToken(claims);

        // 4. 返回 Token 给前端
        // 以后前端会把这个字符串存在本地（比如 LocalStorage），每次请求都放在 Header 里传过来
        return Result.success(token);
    }

    /**
     * 内部私有方法：执行新用户的静默注册
     */
    private User registerUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        // 给新用户起一个默认昵称，比如：pet_123456
        user.setNickname("铲屎官_" + RandomUtil.randomNumbers(6));
        user.setStatus(1); // 状态设为正常

        // 执行插入数据库
        userMapper.insert(user);
        return user;
    }

    /**
     * 测试接口：自动往数据库插入一条数据，并查询出来返回给前端
     */
    @GetMapping("/test")
    public Result<User> testInsertAndQuery() {

        // 1. 造一个测试用户
        User user = new User();
        user.setPhone("13800138002");
        user.setNickname("测试铲屎官4");

        // 2. 执行插入！(注意：这里根本没写 SQL)
        userMapper.insert(user);

        // 3. 插入成功后，MyBatis-Plus 会自动把生成的自增主键 ID 回填到 user 对象里！
        // 我们利用这个 ID，再从数据库里把它原封不动地查出来
        User dbUser = userMapper.selectById(user.getId());

        // 4. 用咱们的大厂规范 Result 包装好，扔给浏览器！
        return Result.success(dbUser);
    }
}
