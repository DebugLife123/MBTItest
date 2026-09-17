package com.debuglife.mbti.common.security;

import com.debuglife.mbti.common.api.ProblemDetailWriter;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ProblemDetailWriter problemDetailWriter;
    private final RateLimiterRegistry registry;
    private final boolean enabled;
    private final int capacity;
    private final Duration refillPeriod;
    private final Map<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    public RateLimitFilter(ProblemDetailWriter problemDetailWriter,
                           RateLimiterRegistry registry,
                           @Value("${app.security.rate-limit.enabled:true}") boolean enabled,
                           @Value("${app.security.rate-limit.capacity:120}") int capacity,
                           @Value("${app.security.rate-limit.refill-seconds:60}") long refillSeconds) {
        this.problemDetailWriter = problemDetailWriter;
        this.registry = registry;
        this.enabled = enabled;
        this.capacity = capacity;
        this.refillPeriod = Duration.ofSeconds(Math.max(refillSeconds, 1));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !enabled || !path.startsWith("/api/") || path.startsWith("/api/v1/auth/")
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        RateLimiter limiter = limiters.computeIfAbsent(clientKey(request), this::createLimiter);
        if (!limiter.acquirePermission()) {
            response.setHeader("Retry-After", Long.toString(Math.max(refillPeriod.toSeconds(), 1)));
            problemDetailWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                    "RATE_LIMITED", "请求过于频繁，请稍后重试");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private RateLimiter createLimiter(String key) {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(capacity)
                .limitRefreshPeriod(refillPeriod)
                .timeoutDuration(Duration.ZERO)
                .build();
        return registry.rateLimiter(key, config);
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
