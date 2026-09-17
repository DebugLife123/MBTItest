package com.debuglife.mbti.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PersonalityRequest {

    @NotBlank(message = "类型代码不能为空")
    @Size(min = 4, max = 4, message = "类型代码必须是 4 位字母")
    private String typeCode;

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 100, message = "类型名称不能超过 100 字")
    private String typeName;

    private String description;
    private String strengths;
    private String weaknesses;
    private String careerSuggestions;

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }

    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }

    public String getCareerSuggestions() { return careerSuggestions; }
    public void setCareerSuggestions(String careerSuggestions) { this.careerSuggestions = careerSuggestions; }
}
