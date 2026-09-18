package com.debuglife.mbti.ai.dto;

import jakarta.validation.constraints.Size;
import java.util.List;

public class TeamAnalysisRequest {

    @Size(max = 500, message = "人数不能超过500")
    private List<String> typeCodes;

    @Size(max = 500, message = "关注点不能超过500字")
    private String focus;

    public List<String> getTypeCodes() { return typeCodes; }
    public void setTypeCodes(List<String> typeCodes) { this.typeCodes = typeCodes; }
    public String getFocus() { return focus; }
    public void setFocus(String focus) { this.focus = focus; }
}