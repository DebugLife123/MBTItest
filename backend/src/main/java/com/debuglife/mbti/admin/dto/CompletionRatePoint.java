package com.debuglife.mbti.admin.dto;

import java.time.LocalDate;

public class CompletionRatePoint {
    private LocalDate date;
    private Long totalAttempts;
    private Long completedAttempts;
    private Double completionRate;

    public CompletionRatePoint() {
    }

    public CompletionRatePoint(LocalDate date, Long totalAttempts, Long completedAttempts, Double completionRate) {
        this.date = date;
        this.totalAttempts = totalAttempts;
        this.completedAttempts = completedAttempts;
        this.completionRate = completionRate;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Long getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(Long totalAttempts) { this.totalAttempts = totalAttempts; }
    public Long getCompletedAttempts() { return completedAttempts; }
    public void setCompletedAttempts(Long completedAttempts) { this.completedAttempts = completedAttempts; }
    public Double getCompletionRate() { return completionRate; }
    public void setCompletionRate(Double completionRate) { this.completionRate = completionRate; }
}
