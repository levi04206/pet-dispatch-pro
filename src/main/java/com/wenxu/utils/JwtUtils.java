package com.wenxu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import java.util.Map;

public class JwtUtils {
    // 签名密钥（一定要保密，就像保险箱钥匙）
    private static final String SIGN_KEY = "wenxu_pet_dispatch_pro_secret_key_2026";
    // 过期时间：设长一点，比如 7 天，方便咱们测试
    private static final Long EXPIRE_TIME = 604800000L;

    /**
     * 生成令牌
     * @param claims 想要存在 Token 里的信息（比如 userId）
     */
    public static String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims) // 把用户信息塞进去
                .signWith(SignatureAlgorithm.HS256, SIGN_KEY) // 签名加密
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME)) // 设置有效期
                .compact();
    }

    /**
     * 解析令牌
     * @param token 令牌字符串
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SIGN_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}