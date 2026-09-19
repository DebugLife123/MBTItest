package com.debuglife.mbti.auth.api;

import com.debuglife.mbti.auth.dto.AuthResponse;
import com.debuglife.mbti.auth.dto.LoginRequest;
import com.debuglife.mbti.auth.dto.RefreshTokenRequest;
import com.debuglife.mbti.auth.dto.RegisterRequest;
import com.debuglife.mbti.auth.entity.User;
import com.debuglife.mbti.auth.service.AuthService;
import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.audit.AuditAction;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @AuditAction(value = "AUTH_REGISTER", description = "用户注册")
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @AuditAction(value = "AUTH_LOGIN", description = "用户登录")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @AuditAction(value = "AUTH_REFRESH", description = "刷新令牌")
    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @GetMapping("/me")
    public ApiResponse<AuthResponse.UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = authService.getCurrentUser(authentication.getName());
        return ApiResponse.success(AuthResponse.UserDTO.fromEntity(user));
    }

    @AuditAction(value = "AUTH_LOGOUT", description = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success(null);
    }
}
