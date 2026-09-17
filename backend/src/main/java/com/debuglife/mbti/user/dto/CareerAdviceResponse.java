package com.debuglife.mbti.user.dto;

import java.util.List;

public class CareerAdviceResponse {
    private String typeCode;
    private String typeName;
    private String summary;
    private String careerSuggestions;
    private List<String> recommendedRoles;
    private List<String> skillSuggestions;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCareerSuggestions() { return careerSuggestions; }
    public void setCareerSuggestions(String careerSuggestions) { this.careerSuggestions = careerSuggestions; }

    public List<String> getRecommendedRoles() { return recommendedRoles; }
    public void setRecommendedRoles(List<String> recommendedRoles) { this.recommendedRoles = recommendedRoles; }

    public List<String> getSkillSuggestions() { return skillSuggestions; }
    public void setSkillSuggestions(List<String> skillSuggestions) { this.skillSuggestions = skillSuggestions; }
}
