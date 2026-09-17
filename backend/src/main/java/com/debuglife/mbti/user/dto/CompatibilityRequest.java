package com.debuglife.mbti.user.dto;

import jakarta.validation.constraints.NotBlank;

public class CompatibilityRequest {
    @NotBlank(message = "对方用户名不能为空")
    private String otherUsername;

    public String getOtherUsername() { return otherUsername; }
    public void setOtherUsername(String otherUsername) { this.otherUsername = otherUsername; }
}
