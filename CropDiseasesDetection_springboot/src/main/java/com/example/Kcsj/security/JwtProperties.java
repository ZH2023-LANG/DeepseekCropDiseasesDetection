package com.example.Kcsj.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProperties {
    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-expire-seconds:900}")
    private long accessExpireSeconds;

    @Value("${security.jwt.refresh-expire-seconds:604800}")
    private long refreshExpireSeconds;

    public String getSecret() {
        return secret;
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }

    public long getRefreshExpireSeconds() {
        return refreshExpireSeconds;
    }
}

