package com.example.Kcsj.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenService {
    private final JwtProperties jwtProperties;
    private Key key;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateAccessToken(Integer userId, String username, String role) {
        return buildToken(userId, username, role, "access", jwtProperties.getAccessExpireSeconds());
    }

    public String generateRefreshToken(Integer userId, String username, String role) {
        return buildToken(userId, username, role, "refresh", jwtProperties.getRefreshExpireSeconds());
    }

    private String buildToken(Integer userId, String username, String role, String type, long expireSeconds) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireSeconds * 1000L);
        return Jwts.builder()
            .setId(UUID.randomUUID().toString().replace("-", ""))
            .setSubject(String.valueOf(userId))
            .claim("username", username)
            .claim("role", role)
            .claim("type", type)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    }

    public Jws<Claims> parse(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}

