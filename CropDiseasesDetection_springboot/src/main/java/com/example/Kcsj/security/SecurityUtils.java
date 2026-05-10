package com.example.Kcsj.security;

import com.example.Kcsj.common.ApiException;
import com.example.Kcsj.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static UserPrincipal currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "unauthorized");
        }
        return (UserPrincipal) authentication.getPrincipal();
    }

    public static Integer currentUserId() {
        return currentUser().getUserId();
    }

    public static String currentRole() {
        return currentUser().getRole();
    }

    public static boolean isAdmin() {
        return "admin".equalsIgnoreCase(currentRole());
    }
}

