package com.debuglife.mbti.admin.dto;

import java.time.LocalDateTime;

public class UserStatistics {
    private Long totalUsers;
    private Long activeUsers;
    private Long totalAttempts;
    private Long completedAttempts;
    private Double completionRate;
    private LocalDateTime lastUpdated;

    public UserStatistics() {
    }

    public UserStatistics(Long totalUsers, Long activeUsers, Long totalAttempts, 
                         Long completedAttempts, Double completionRate) {
        this.totalUsers = totalUsers;
        this.activeUsers = activeUsers;
        this.totalAttempts = totalAttempts;
        this.completedAttempts = completedAttempts;
        this.completionRate = completionRate;
        this.lastUpdated = LocalDateTime.now();
    }

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(Long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }

    public Long getCompletedAttempts() {
        return completedAttempts;
    }

    public void setCompletedAttempts(Long completedAttempts) {
        this.completedAttempts = completedAttempts;
    }

    public Double getCompletionRate() {
        return completionRate;
    }

    public void setCompletionRate(Double completionRate) {
        this.completionRate = completionRate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
