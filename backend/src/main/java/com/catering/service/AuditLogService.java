package com.catering.service;

import com.catering.entity.AuditLog;
import com.catering.mapper.AuditLogMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogMapper auditLogMapper;

    public AuditLogService(AuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    @Async
    public void saveAuditLog(AuditLog auditLog) {
        auditLogMapper.insert(auditLog);
    }
}
