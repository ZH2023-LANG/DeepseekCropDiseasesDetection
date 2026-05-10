package com.example.Kcsj.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.Kcsj.api.v1.auth.dto.ChangePasswordRequest;
import com.example.Kcsj.api.v1.auth.dto.LoginRequest;
import com.example.Kcsj.api.v1.auth.dto.LoginResponse;
import com.example.Kcsj.api.v1.auth.dto.UserProfile;
import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import com.example.Kcsj.entity.User;
import com.example.Kcsj.mapper.UserMapper;
import com.example.Kcsj.security.InMemoryRefreshTokenStore;
import com.example.Kcsj.security.JwtProperties;
import com.example.Kcsj.security.JwtTokenService;
import com.example.Kcsj.security.RefreshSession;
import com.example.Kcsj.security.SecurityUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final JwtTokenService jwtTokenService;
    private final InMemoryRefreshTokenStore refreshTokenStore;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public AuthService(UserMapper userMapper,
                       JwtTokenService jwtTokenService,
                       InMemoryRefreshTokenStore refreshTokenStore,
                       JwtProperties jwtProperties,
                       PasswordEncoder passwordEncoder,
                       AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenStore = refreshTokenStore;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            auditLogService.tryLog(null, "LOGIN_FAILED", "USER", null, "username=" + request.getUsername());
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "user not found");
        }

        if (!matches(user, request.getPassword())) {
            auditLogService.tryLog(user.getId(), "LOGIN_FAILED", "USER", user.getId().toString(), "password mismatch");
            throw new ApiException(ErrorCode.PASSWORD_MISMATCH, "password mismatch");
        }

        migratePlaintextPasswordIfNeeded(user, request.getPassword());
        LoginResponse response = issueTokens(user);
        auditLogService.tryLog(user.getId(), "LOGIN_SUCCESS", "USER", user.getId().toString(), "login success");
        return response;
    }

    public LoginResponse refresh(String refreshToken) {
        Jws<Claims> jws = jwtTokenService.parse(refreshToken);
        Claims claims = jws.getBody();
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, "invalid refresh token");
        }
        RefreshSession session = refreshTokenStore.getValidSessionOrNull(jws);
        if (session == null) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, "refresh token revoked or expired");
        }
        User user = userMapper.selectById(session.getUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "user not found");
        }
        refreshTokenStore.revokeByJti(claims.getId());
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        Jws<Claims> jws = jwtTokenService.parse(refreshToken);
        String jti = jws.getBody().getId();
        refreshTokenStore.revokeByJti(jti);
        auditLogService.tryLog(SecurityUtils.currentUserId(), "LOGOUT", "TOKEN", jti, "logout");
    }

    public UserProfile me() {
        User user = userMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "user not found");
        }
        return toProfile(user);
    }

    public void changePassword(ChangePasswordRequest request) {
        User user = userMapper.selectById(SecurityUtils.currentUserId());
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND, "user not found");
        }
        if (!matches(user, request.getOldPassword())) {
            throw new ApiException(ErrorCode.PASSWORD_MISMATCH, "old password mismatch");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userMapper.updateById(user);
        auditLogService.tryLog(user.getId(), "CHANGE_PASSWORD", "USER", user.getId().toString(), "password changed");
    }

    public void encodePasswordForNewUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
    }

    private LoginResponse issueTokens(User user) {
        String accessToken = jwtTokenService.generateAccessToken(user.getId(), user.getUsername(), user.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(user.getId(), user.getUsername(), user.getRole());
        Jws<Claims> refreshClaims = jwtTokenService.parse(refreshToken);
        RefreshSession refreshSession = new RefreshSession();
        refreshSession.setUserId(user.getId());
        refreshSession.setUsername(user.getUsername());
        refreshSession.setRole(user.getRole());
        refreshSession.setExpireAt(LocalDateTime.now().plusSeconds(jwtProperties.getRefreshExpireSeconds()));
        refreshSession.setRevoked(false);
        refreshTokenStore.save(refreshClaims.getBody().getId(), refreshSession);

        LoginResponse response = new LoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setAccessExpiresIn(jwtProperties.getAccessExpireSeconds());
        response.setRefreshExpiresIn(jwtProperties.getRefreshExpireSeconds());
        response.setUser(toProfile(user));
        return response;
    }

    private boolean matches(User user, String rawPassword) {
        if (user.getPassword() == null) {
            return false;
        }
        if (isBcrypt(user.getPassword())) {
            return passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return Objects.equals(rawPassword, user.getPassword());
    }

    private void migratePlaintextPasswordIfNeeded(User user, String rawPassword) {
        if (!isBcrypt(user.getPassword()) && Objects.equals(rawPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            userMapper.updateById(user);
        }
    }

    private boolean isBcrypt(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private UserProfile toProfile(User user) {
        UserProfile profile = new UserProfile();
        profile.setId(user.getId());
        profile.setUsername(user.getUsername());
        profile.setName(user.getName());
        profile.setRole(user.getRole());
        profile.setAvatar(user.getAvatar());
        profile.setEmail(user.getEmail());
        profile.setTel(user.getTel());
        return profile;
    }
}

