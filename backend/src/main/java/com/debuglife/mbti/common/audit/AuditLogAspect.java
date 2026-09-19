package com.debuglife.mbti.common.audit;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

/**
 * 操作日志 AOP 切面。
 *
 * <p>拦截所有标注 {@link AuditAction} 的 Controller 方法，在方法执行后异步无侵入地
 * 落一条审计记录：操作人、动作、路径、耗时、成败与失败原因。请求参数做脱敏处理——
 * 任何包含 password / token / secret / key 字样的字段都以 *** 代替。</p>
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);
    private static final int DETAIL_MAX = 900;

    private final AuditLogRepository repository;

    public AuditLogAspect(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Around("@annotation(auditAction)")
    public Object around(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        long start = System.currentTimeMillis();
        AuditLog entry = baseEntry(auditAction);
        entry.setDetail(maskedDetail(joinPoint.getArgs()));
        try {
            Object result = joinPoint.proceed();
            entry.setSuccess(true);
            entry.setCostMs(System.currentTimeMillis() - start);
            saveQuietly(entry);
            return result;
        } catch (Throwable ex) {
            entry.setSuccess(false);
            entry.setErrorMessage(truncate(ex.getMessage(), 500));
            entry.setCostMs(System.currentTimeMillis() - start);
            saveQuietly(entry);
            throw ex;
        }
    }

    private AuditLog baseEntry(AuditAction auditAction) {
        AuditLog entry = new AuditLog();
        entry.setAction(auditAction.value());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            entry.setUsername(auth.getName());
        }
        HttpServletRequest request = currentRequest();
        if (request != null) {
            entry.setHttpMethod(request.getMethod());
            entry.setPath(request.getRequestURI());
            entry.setIp(clientIp(request));
        } else {
            entry.setHttpMethod("-");
            entry.setPath("-");
        }
        return entry;
    }

    /** 序列化入参并脱敏，密码/令牌类字段不出现在日志里 */
    @SuppressWarnings("unused")
    private String maskedDetail(Object[] args) {
        if (args == null || args.length == 0) return null;
        StringBuilder sb = new StringBuilder();
        for (Object arg : args) {
            if (arg == null || arg instanceof HttpServletRequest || arg instanceof MultipartFile) continue;
            String text = String.valueOf(arg);
            text = text.replaceAll("(?i)(password|token|secret|apiKey|api_key)\s*[=:]\s*[^,}\s]+", "$1=***");
            if (sb.length() > 0) sb.append(" | ");
            sb.append(text);
            if (sb.length() > DETAIL_MAX) break;
        }
        return truncate(sb.toString(), DETAIL_MAX);
    }

    private void saveQuietly(AuditLog entry) {
        try {
            repository.save(entry);
        } catch (Exception ex) {
            // 审计失败绝不影响主流程
            log.warn("审计日志写入失败: {}", ex.getMessage());
        }
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            return attrs.getRequest();
        }
        return null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String truncate(String text, int max) {
        if (text == null) return null;
        return text.length() <= max ? text : text.substring(0, max);
    }
}
