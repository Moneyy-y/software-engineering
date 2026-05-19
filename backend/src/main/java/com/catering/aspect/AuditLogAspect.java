package com.catering.aspect;

import com.catering.context.UserContext;
import com.catering.entity.AuditLog;
import com.catering.service.AuditLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditLogAspect {
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public AuditLogAspect(AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    @Pointcut("@annotation(com.catering.annotation.AuditLog)")
    public void logPointcut() {
    }

    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } finally {
            saveLog(joinPoint);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) return;
            HttpServletRequest request = attributes.getRequest();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            AuditLog auditLog = new AuditLog();

            Long userId = UserContext.getUserId();
            if (userId != null) {
                auditLog.setUserId(userId);
            }

            com.catering.annotation.AuditLog annotation = signature.getMethod().getAnnotation(com.catering.annotation.AuditLog.class);
            if (annotation != null) {
                auditLog.setOperation(annotation.value());
            }

            auditLog.setMethod(signature.getDeclaringTypeName() + "." + signature.getName());
            
            Object[] args = joinPoint.getArgs();
            if (args.length > 0) {
                auditLog.setParams(objectMapper.writeValueAsString(args));
            }

            auditLog.setIp(getIpAddr(request));
            auditLog.setCreateTime(LocalDateTime.now());

            auditLogService.saveAuditLog(auditLog);
        } catch (Exception e) {
        }
    }

    private String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
