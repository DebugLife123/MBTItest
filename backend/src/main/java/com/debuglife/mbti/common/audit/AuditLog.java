package com.debuglife.mbti.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * 操作审计日志实体，对应 {@code sys_audit_log} 表。
 *
 * <p>只追加不更新，供管理端审计查询与安全追溯使用。</p>
 */
@Entity
@Table(name = "sys_audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "action", nullable = false, length = 64)
    private String action;

    @Column(name = "detail", length = 1024)
    private String detail;

    @Column(name = "http_method", nullable = false, length = 8)
    private String httpMethod;

    @Column(name = "path", nullable = false)
    private String path;

    @Column(name = "ip", length = 64)
    private String ip;

    @Column(name = "success", nullable = false)
    private Boolean success = true;

    @Column(name = "error_message", length = 512)
    private String errorMessage;

    @Column(name = "cost_ms")
    private Long costMs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public String getHttpMethod() { return httpMethod; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getCostMs() { return costMs; }
    public void setCostMs(Long costMs) { this.costMs = costMs; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
