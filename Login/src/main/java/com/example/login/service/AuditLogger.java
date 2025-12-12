package com.example.login.service;

import com.example.login.entity.AuditLog;
import com.example.login.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuditLogger {

    private final AuditLogRepository auditLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void log(String action, String performedBy, String metadata) {
        AuditLog a = AuditLog.builder()
                .action(action)
                .performedBy(performedBy)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();
        auditLogRepository.save(a);
        // push a lightweight representation to redis list for fast read
        redisTemplate.opsForList().leftPush("audit_logs", action + "|" + performedBy + "|" + metadata + "|" + a.getTimestamp());
    }
}
