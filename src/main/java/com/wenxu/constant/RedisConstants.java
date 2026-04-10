package com.wenxu.constant;

/**
 * Redis 统一 Key 与过期时间管理中心
 * 规范：所有写进 Redis 的 Key 前缀和 TTL，必须在这里报备！
 */
public class RedisConstants {

    // --- 登录模块 ---

    /** 登录验证码的 Key 前缀 */
    public static final String LOGIN_CODE_KEY = "pet:login:code:";

    /** 登录验证码的过期时间 (分钟) */
    public static final Long LOGIN_CODE_TTL = 5L;

    // (以后如果有 token、订单缓存等，全往这里面加)
}