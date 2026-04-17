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

    // --- 宠托师推荐模块 ---

    /** 宠托师评分排行榜，score 为综合评分，member 为 sitterId */
    public static final String SITTER_RANK_RATING_KEY = "pet:sitter:rank:rating";
}
