package com.debuglife.mbti.system.api;

import com.debuglife.mbti.common.api.ApiResponse;
import com.debuglife.mbti.common.api.TraceIdFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "System", description = "Runtime and bootstrap endpoints")
public class SystemController {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final String applicationName;

    public SystemController(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate,
                            @Value("${spring.application.name}") String applicationName) {
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.applicationName = applicationName;
    }

    @GetMapping("/health")
    @Operation(summary = "Application dependency health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health(HttpServletRequest request) {
        Map<String, Object> dependencies = new LinkedHashMap<>();
        dependencies.put("database", checkDatabase());
        dependencies.put("redis", checkRedis());
        dependencies.put("application", applicationName);
        dependencies.put("phase", "foundation");
        dependencies.put("time", OffsetDateTime.now());
        return ResponseEntity.ok(ApiResponse.success("Foundation is ready", dependencies,
                request.getRequestURI(), String.valueOf(request.getAttribute(TraceIdFilter.ATTRIBUTE))));
    }

    @GetMapping("/bootstrap")
    @Operation(summary = "Frontend bootstrap metadata")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bootstrap(HttpServletRequest request) {
        Map<String, Object> payload = Map.of(
                "productName", "MBTI AI Assessment Platform",
                "apiVersion", "v1",
                "phase", "Foundation",
                "features", new String[]{"Vue 3", "Spring Boot", "MySQL", "Redis", "Flyway", "OpenAPI"}
        );
        return ResponseEntity.ok(ApiResponse.success(payload, request.getRequestURI(),
                String.valueOf(request.getAttribute(TraceIdFilter.ATTRIBUTE))));
    }

    private String checkDatabase() {
        try { return Integer.valueOf(1).equals(jdbcTemplate.queryForObject("SELECT 1", Integer.class)) ? "UP" : "DOWN"; }
        catch (Exception ignored) { return "DOWN"; }
    }

    private String checkRedis() {
        try { return "PONG".equals(redisTemplate.getConnectionFactory().getConnection().ping()) ? "UP" : "DOWN"; }
        catch (Exception ignored) { return "DOWN"; }
    }
}
