package com.camping.system.util;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.enums.UserRole;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public final class TokenUtils {

    private TokenUtils() {
    }

    public static String issueToken(Long userId,
                                    String username,
                                    String displayName,
                                    UserRole role,
                                    String secret,
                                    long expireDays) {
        long expireAt = Instant.now().plus(expireDays, ChronoUnit.DAYS).getEpochSecond();
        String payload = userId + "|" + username + "|" + displayName + "|" + role.name() + "|" + expireAt;
        String signature = sign(payload, secret);
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((payload + "|" + signature).getBytes(StandardCharsets.UTF_8));
    }

    public static AuthContext.AuthUser parseToken(String token, String secret) {
        try {
            String decoded = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", 6);
            if (parts.length != 6) {
                throw new BusinessException(401, "无效的登录凭证");
            }
            String payload = String.join("|", parts[0], parts[1], parts[2], parts[3], parts[4]);
            String signature = sign(payload, secret);
            if (!signature.equals(parts[5])) {
                throw new BusinessException(401, "登录凭证校验失败");
            }
            long expireAt = Long.parseLong(parts[4]);
            if (Instant.now().getEpochSecond() > expireAt) {
                throw new BusinessException(401, "登录已过期，请重新登录");
            }
            return new AuthContext.AuthUser(
                    Long.parseLong(parts[0]),
                    parts[1],
                    parts[2],
                    UserRole.valueOf(parts[3])
            );
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(401, "登录凭证格式不正确");
        }
    }

    private static String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign token", ex);
        }
    }
}
