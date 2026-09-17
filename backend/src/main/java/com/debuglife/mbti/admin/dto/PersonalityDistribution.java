package com.debuglife.mbti.admin.dto;

public class PersonalityDistribution {
    private String personalityType;
    private Long count;
    private Double percentage;

    public PersonalityDistribution() {
    }

    public PersonalityDistribution(String personalityType, Long count, Double percentage) {
        this.personalityType = personalityType;
        this.count = count;
        this.percentage = percentage;
    }

    public String getPersonalityType() {
        return personalityType;
    }

    public void setPersonalityType(String personalityType) {
        this.personalityType = personalityType;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
