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

    private void saveLog(ProceedingJoinPoint point, AuditLog auditLog, Long costTime, int status){
        try{
            ServletRequestAttributes attributes = (ServletRequestAttributes)
                    RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            org.example.enterprisebacksystem.domain.AuditLog logEntity = new
                    org.example.enterprisebacksystem.domain.AuditLog();

            logEntity.setAction(auditLog.value());
            logEntity.setMethod(request.getMethod());
            logEntity.setUrl(request.getRequestURL().toString());
            logEntity.setIpAddress(getClientIP(request));
            logEntity.setCostTime(costTime);
            logEntity.setStatus(status);
            logEntity.setCreateTime(LocalDateTime.now());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if(authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
                logEntity.setUserId(loginUser.getUserId());
                logEntity.setUserName(loginUser.getUsername());
            }

            String params = objectMapper.writeValueAsString(point.getArgs());
            logEntity.setParams(params);

            auditLogMapper.insert(logEntity);
        }
        catch(Exception e){
            log.error("审计日志记录失败", e);
        }

    }

    private String makeSensitiveParams(String params) {
        if(params == null) {
            return null;
        }

        String lower = params.toLowerCase();
        if (lower.contains("password") || lower.contains("token")) {
            return "{\"message\":\"参数包含敏感信息，已隐藏\"}";
        }

        if(params.length() > 2000) {
            return params.substring(0, 2000);
        }

        return params;
    }


    private String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forward-For");

        if(ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)){
            return ip.split(",")[0];
        }

        ip = request.getHeader("X-Real-IP");
        if(ip != null && !ip.isBlank() && !"unknown".equalsIgnoreCase(ip)){
            return ip;
        }

        return request.getRemoteAddr();
    }
}
