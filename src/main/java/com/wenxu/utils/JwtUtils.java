package com.wenxu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    private final String signKey;
    private final Long expireTime;

    public JwtUtils(@Value("${wenxu.jwt.sign-key}") String signKey,
                    @Value("${wenxu.jwt.expire-time}") Long expireTime) {
        this.signKey = signKey;
        this.expireTime = expireTime;
    }

    public String createToken(Map<String, Object> claims) {
        return Jwts.builder()
                .addClaims(claims)
                .signWith(SignatureAlgorithm.HS256, signKey)
                .setExpiration(new Date(System.currentTimeMillis() + expireTime))
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(signKey)
                .parseClaimsJws(token)
                .getBody();
    }
}
