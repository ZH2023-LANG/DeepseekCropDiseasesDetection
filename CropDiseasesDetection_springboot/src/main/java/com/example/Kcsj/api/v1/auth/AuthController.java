package com.example.Kcsj.api.v1.auth;

import com.example.Kcsj.api.v1.auth.dto.ChangePasswordRequest;
import com.example.Kcsj.api.v1.auth.dto.LoginRequest;
import com.example.Kcsj.api.v1.auth.dto.LoginResponse;
import com.example.Kcsj.api.v1.auth.dto.RefreshRequest;
import com.example.Kcsj.api.v1.auth.dto.UserProfile;
import com.example.Kcsj.common.ApiResponse;
import com.example.Kcsj.service.AuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.getRefreshToken());
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserProfile> me() {
        return ApiResponse.success(authService.me());
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ApiResponse.success(null);
    }
}

