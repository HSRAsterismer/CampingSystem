package com.camping.system.dto;

import com.camping.system.enums.UserRole;
import jakarta.validation.constraints.NotBlank;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "密码不能为空") String password
    ) {
    }

    public record UserProfile(
            Long id,
            String username,
            String displayName,
            String phone,
            UserRole role
    ) {
    }

    public record LoginResponse(
            String token,
            UserProfile user
    ) {
    }
}
