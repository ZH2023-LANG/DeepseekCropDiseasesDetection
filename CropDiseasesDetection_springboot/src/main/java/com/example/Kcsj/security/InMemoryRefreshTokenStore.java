package com.example.Kcsj.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryRefreshTokenStore {
    private final Map<String, RefreshSession> sessions = new ConcurrentHashMap<>();

    public void save(String jti, RefreshSession session) {
        sessions.put(jti, session);
    }

    public RefreshSession getValidSessionOrNull(Jws<Claims> claimsJws) {
        String jti = claimsJws.getBody().getId();
        RefreshSession session = sessions.get(jti);
        if (session == null) {
            return null;
        }
        if (session.isRevoked()) {
            return null;
        }
        if (session.getExpireAt().isBefore(LocalDateTime.now())) {
            sessions.remove(jti);
            return null;
        }
        return session;
    }

    public void revokeByJti(String jti) {
        RefreshSession session = sessions.get(jti);
        if (session != null) {
            session.setRevoked(true);
        }
    }

    @Scheduled(fixedDelay = 300000)
    public void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        sessions.entrySet().removeIf(entry -> {
            RefreshSession session = entry.getValue();
            return session == null || session.isRevoked() || session.getExpireAt().isBefore(now);
        });
    }
}

