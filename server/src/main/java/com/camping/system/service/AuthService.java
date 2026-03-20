package com.camping.system.service;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.dto.AuthDtos;
import com.camping.system.entity.User;
import com.camping.system.repository.UserRepository;
import com.camping.system.util.PasswordUtils;
import com.camping.system.util.TokenUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    @Value("${app.auth.token-secret}")
    private String tokenSecret;

    @Value("${app.auth.expire-days}")
    private long expireDays;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest request) {
        User user = userRepository.findByUsernameAndEnabledTrue(request.username())
                .orElseThrow(() -> new BusinessException(401, "用户名或密码错误"));
        if (!PasswordUtils.matches(user.getUsername(), request.password(), user.getPasswordHash())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        user.setLastLoginAt(java.time.LocalDateTime.now());
        userRepository.save(user);

        String token = TokenUtils.issueToken(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                tokenSecret,
                expireDays
        );
        return new AuthDtos.LoginResponse(token, toProfile(user));
    }

    public AuthContext.AuthUser parseToken(String token) {
        AuthContext.AuthUser authUser = TokenUtils.parseToken(token, tokenSecret);
        User user = userRepository.findById(authUser.userId())
                .filter(User::getEnabled)
                .orElseThrow(() -> new BusinessException(401, "账号不存在或已被禁用"));
        return new AuthContext.AuthUser(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole());
    }

    public AuthDtos.UserProfile getCurrentProfile() {
        AuthContext.AuthUser authUser = AuthContext.getRequiredUser();
        User user = userRepository.findById(authUser.userId())
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return toProfile(user);
    }

    private AuthDtos.UserProfile toProfile(User user) {
        return new AuthDtos.UserProfile(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getPhone(),
                user.getRole()
        );
    }
}
