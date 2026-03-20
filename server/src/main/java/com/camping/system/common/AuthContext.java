package com.camping.system.common;

import com.camping.system.enums.UserRole;

public final class AuthContext {

    private static final ThreadLocal<AuthUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthUser authUser) {
        HOLDER.set(authUser);
    }

    public static AuthUser getRequiredUser() {
        AuthUser authUser = HOLDER.get();
        if (authUser == null) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
        return authUser;
    }

    public static void clear() {
        HOLDER.remove();
    }

    public record AuthUser(Long userId, String username, String displayName, UserRole role) {

        public boolean isAdmin() {
            return role == UserRole.ADMIN;
        }
    }
}
