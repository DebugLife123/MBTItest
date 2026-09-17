package com.debuglife.mbti.user.dto;

import java.util.List;

public class CompatibilityResponse {
    private String myType;
    private String otherType;
    private String otherUsername;
    private Integer score;
    private String level;
    private String summary;
    private List<String> sharedTraits;
    private List<String> complementaryTraits;
    private List<String> watchOuts;

    public String getMyType() { return myType; }
    public void setMyType(String myType) { this.myType = myType; }

    public String getOtherType() { return otherType; }
    public void setOtherType(String otherType) { this.otherType = otherType; }

    public String getOtherUsername() { return otherUsername; }
    public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public List<String> getSharedTraits() { return sharedTraits; }
    public void setSharedTraits(List<String> sharedTraits) { this.sharedTraits = sharedTraits; }

    public List<String> getComplementaryTraits() { return complementaryTraits; }
    public void setComplementaryTraits(List<String> complementaryTraits) { this.complementaryTraits = complementaryTraits; }

    public List<String> getWatchOuts() { return watchOuts; }
    public void setWatchOuts(List<String> watchOuts) { this.watchOuts = watchOuts; }
}
