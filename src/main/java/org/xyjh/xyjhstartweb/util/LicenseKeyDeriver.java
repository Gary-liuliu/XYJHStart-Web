package org.xyjh.xyjhstartweb.util;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class LicenseKeyDeriver {

    /**
     * 将任意字符串 licenseKey 安全地派生为符合 HS256 要求的 SecretKey（256 位）
     */
    public static SecretKey deriveHS256Key(String licenseKey) {
        try {
            byte[] raw = licenseKey.getBytes(StandardCharsets.UTF_8);
            // 如果原始字节 >= 32，截断；否则用 SHA-256 扩展（推荐方式）
            byte[] keyBytes = raw.length >= 32 
                ? java.util.Arrays.copyOf(raw, 32)
                : MessageDigest.getInstance("SHA-256").digest(raw);
            return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive key from licenseKey", e);
        }
    }
}