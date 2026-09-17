package com.debuglife.mbti.assessment.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SubmitAnswersRequest {
    @NotEmpty(message = "答案不能为空")
    private List<AnswerItem> answers;

    @Data
    @AllArgsConstructor
    public static class AnswerItem {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;
        
        @NotNull(message = "答案不能为空")
        private String answer;
    }
}
