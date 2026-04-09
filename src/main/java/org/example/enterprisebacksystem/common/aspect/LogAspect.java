package org.example.enterprisebacksystem.common.aspect;

import java.time.LocalDateTime;
import java.util.Arrays;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.example.enterprisebacksystem.common.annotation.AuditLog;

import org.example.enterprisebacksystem.mapper.AuditLogMapper;
import org.example.enterprisebacksystem.security.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LogAspect {

    // LogMapper
    private final AuditLogMapper auditLogMapper;
    // 序列化参数，objectMapper
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint point, AuditLog auditLog) throws Throwable{
        long beginTime = System.currentTimeMillis();
        int status = 1;

        try {
            return point.proceed();
        }
        catch (Throwable e) {
            status = 0;
            throw e;
        }
        finally {
            long costTime = System.currentTimeMillis() - beginTime;
            saveLog(point, auditLog, costTime, status);
        }
    }
}
