package com.debuglife.mbti.user.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GrowthPoint {
    private Long resultId;
    private String typeCode;
    private String typeName;
    private Integer eScore;
    private Integer iScore;
    private Integer sScore;
    private Integer nScore;
    private Integer tScore;
    private Integer fScore;
    private Integer jScore;
    private Integer pScore;
    private LocalDateTime createdAt;

    /** 与上一次测评相比发生变化的维度，例如 ["E/I"] */
    private List<String> changedDimensions;

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public Integer getEScore() { return eScore; }
    public void setEScore(Integer eScore) { this.eScore = eScore; }

    public Integer getIScore() { return iScore; }
    public void setIScore(Integer iScore) { this.iScore = iScore; }

    public Integer getSScore() { return sScore; }
    public void setSScore(Integer sScore) { this.sScore = sScore; }

    public Integer getNScore() { return nScore; }
    public void setNScore(Integer nScore) { this.nScore = nScore; }

    public Integer getTScore() { return tScore; }
    public void setTScore(Integer tScore) { this.tScore = tScore; }

    public Integer getFScore() { return fScore; }
    public void setFScore(Integer fScore) { this.fScore = fScore; }

    public Integer getJScore() { return jScore; }
    public void setJScore(Integer jScore) { this.jScore = jScore; }

    public Integer getPScore() { return pScore; }
    public void setPScore(Integer pScore) { this.pScore = pScore; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<String> getChangedDimensions() { return changedDimensions; }
    public void setChangedDimensions(List<String> changedDimensions) { this.changedDimensions = changedDimensions; }
}
