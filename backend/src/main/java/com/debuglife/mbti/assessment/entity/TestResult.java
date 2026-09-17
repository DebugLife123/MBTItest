package com.debuglife.mbti.assessment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_record")
public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "personality_id", nullable = false)
    private Long personalityId;

    @Column(name = "result_type", nullable = false, length = 4)
    private String typeCode;

    @Column(name = "e_score", nullable = false)
    private Integer eScore = 0;

    @Column(name = "i_score", nullable = false)
    private Integer iScore = 0;

    @Column(name = "s_score", nullable = false)
    private Integer sScore = 0;

    @Column(name = "n_score", nullable = false)
    private Integer nScore = 0;

    @Column(name = "t_score", nullable = false)
    private Integer tScore = 0;

    @Column(name = "f_score", nullable = false)
    private Integer fScore = 0;

    @Column(name = "j_score", nullable = false)
    private Integer jScore = 0;

    @Column(name = "p_score", nullable = false)
    private Integer pScore = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "personality_id", insertable = false, updatable = false)
    private MbtiPersonality personality;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPersonalityId() {
        return personalityId;
    }

    public void setPersonalityId(Long personalityId) {
        this.personalityId = personalityId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getEScore() {
        return eScore;
    }

    public void setEScore(Integer eScore) {
        this.eScore = eScore;
    }

    public Integer getIScore() {
        return iScore;
    }

    public void setIScore(Integer iScore) {
        this.iScore = iScore;
    }

    public Integer getSScore() {
        return sScore;
    }

    public void setSScore(Integer sScore) {
        this.sScore = sScore;
    }

    public Integer getNScore() {
        return nScore;
    }

    public void setNScore(Integer nScore) {
        this.nScore = nScore;
    }

    public Integer getTScore() {
        return tScore;
    }

    public void setTScore(Integer tScore) {
        this.tScore = tScore;
    }

    public Integer getFScore() {
        return fScore;
    }

    public void setFScore(Integer fScore) {
        this.fScore = fScore;
    }

    public Integer getJScore() {
        return jScore;
    }

    public void setJScore(Integer jScore) {
        this.jScore = jScore;
    }

    public Integer getPScore() {
        return pScore;
    }

    public void setPScore(Integer pScore) {
        this.pScore = pScore;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public MbtiPersonality getPersonality() {
        return personality;
    }

    public void setPersonality(MbtiPersonality personality) {
        this.personality = personality;
    }
}
