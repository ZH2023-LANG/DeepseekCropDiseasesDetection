package com.example.Kcsj.service;

import com.example.Kcsj.common.TraceContext;
import com.example.Kcsj.entity.AuditLog;
import com.example.Kcsj.mapper.AuditLogMapper;
import com.example.Kcsj.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    public void tryLog(Integer actorId, String action, String targetType, String targetId, String detailJson) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setActorId(actorId);
            auditLog.setActorRole(safeCurrentRole());
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setDetailJson(detailJson);
            auditLog.setTraceId(TraceContext.getTraceId());
            auditLog.setCreatedAt(LocalDateTime.now());
            auditLogMapper.insert(auditLog);
        } catch (Exception e) {
            log.warn("audit log write failed", e);
        }
    }

    private String safeCurrentRole() {
        try {
            return SecurityUtils.currentRole();
        } catch (Exception ignore) {
            return "anonymous";
        }
    }
}

