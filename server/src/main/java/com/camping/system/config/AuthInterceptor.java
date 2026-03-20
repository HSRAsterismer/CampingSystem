package com.camping.system.config;

import com.camping.system.common.AuthContext;
import com.camping.system.common.BusinessException;
import com.camping.system.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith("/api/") || "/api/auth/login".equals(requestUri)) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            throw new BusinessException(401, "请先登录后再访问");
        }

        String token = header.startsWith("Bearer ") ? header.substring(7) : header;
        AuthContext.set(authService.parseToken(token));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }
}
