package com.camping.system.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PasswordUtils {

    private static final String SALT = "camping-system::";

    private PasswordUtils() {
    }

    public static String hash(String username, String rawPassword) {
        String payload = username + SALT + rawPassword;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", ex);
        }
    }

    public static boolean matches(String username, String rawPassword, String hashed) {
        return hash(username, rawPassword).equals(hashed);
    }
}
