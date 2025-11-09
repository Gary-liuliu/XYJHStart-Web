package org.xyjh.xyjhstartweb.util.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 秘钥保持不变
    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    /**
     * 根据设备ID、许可证密钥和【动态过期时间】生成一个JWT
     *
     * @param deviceId     客户端生成的设备唯一ID
     * @param licenseKey   用户激活时使用的许可证密钥
     * @param expirationDate 许可证的到期日期
     * @return 返回生成的JWT字符串
     */
    public String generateToken(String deviceId, String licenseKey, Date expirationDate) {
        Date now = new Date();

        return Jwts.builder()
                .setSubject(licenseKey)
                .claim("deviceId", deviceId)
                .setIssuedAt(now)
                // 使用管理员设定的过期时间
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY)
                .compact();
    }


    /**
     * 【新增方法】专门为管理员生成Token
     * @param username       管理员的用户名
     * @param expirationDate 过期时间
     * @return 返回生成的JWT字符串
     */
    public String generateAdminToken(String username, Date expirationDate) {
        return Jwts.builder()
                .setSubject(username) // 直接将用户名作为 subject
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY)
                .compact();
    }
    /**
     * 从 JWT 中解析出所有声明 (Claims)
     * @param token 客户端传来的 JWT
     * @return 返回 Claims 对象，解析失败则返回 null
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // Token 解析失败（过期、伪造等）
            return null;
        }
    }

}