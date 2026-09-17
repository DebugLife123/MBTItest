package com.debuglife.mbti.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuestionRequest {

    @NotNull(message = "所属维度不能为空")
    private Long dimensionId;

    @NotBlank(message = "题目内容不能为空")
    @Size(max = 500, message = "题目内容不能超过 500 字")
    private String content;

    @NotBlank(message = "选项A不能为空")
    @Size(max = 200, message = "选项A不能超过 200 字")
    private String optionA;

    @NotBlank(message = "选项B不能为空")
    @Size(max = 200, message = "选项B不能超过 200 字")
    private String optionB;

    @NotBlank(message = "选项A维度不能为空")
    @Size(min = 1, max = 1, message = "选项A维度必须是单个字母")
    private String answerType;

    private Integer sortOrder;

    public Long getDimensionId() { return dimensionId; }
    public void setDimensionId(Long dimensionId) { this.dimensionId = dimensionId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getAnswerType() { return answerType; }
    public void setAnswerType(String answerType) { this.answerType = answerType; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
