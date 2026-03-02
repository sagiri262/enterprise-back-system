package org.example.enterprisebacksystem.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtils {
    // 密钥场地要求必须大于256位，即 256 / 8 = 32个字符
    private static final String SECRET_STRING = "EnterpriseBackSystem_SecretKey_Must_Be_Long_Enough_666";
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    // 限定过期时间 7 天
    private static final long EXPIRATION_TIME = 7L * 24 * 60 * 60 * 1000;

    /*
    * 生成 Tokens
    * @Param userId 用户 ID
    * @Param userName 用户名
    */
    public static String generateToken(Long userId, String username) {
        /*
        * 载荷信息
        * 签发时间
        * 过期时间
        * 用户签名
        * */
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                // 当前时间 + 过期时间 = 过期的那一天那一刻时间
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // 解析 TOKENS
    /*
    * 解析 parser()
    * 验证 verifyWith( 私钥 )
    * 建立 build()
    * 解析签名 token parseSignedClaims( token )
    * 获得缓存 getPayload()
    * */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
