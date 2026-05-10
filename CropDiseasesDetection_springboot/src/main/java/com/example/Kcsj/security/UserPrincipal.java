package com.example.Kcsj.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {
    private final Integer userId;
    private final String username;
    private final String role;

    public UserPrincipal(Integer userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String safeRole = role == null ? "COMMON" : role.toUpperCase();
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + safeRole));
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
