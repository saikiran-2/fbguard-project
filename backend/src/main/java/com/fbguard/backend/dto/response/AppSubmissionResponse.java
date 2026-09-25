package com.fbguard.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AppSubmissionResponse {
    private Long aid;
    private String appname;
    private String appid;
    private String appurl;
    private String appIconUrl;
    private String username;
    private String status;
    private int riskScore;
    private String riskLevel;
    private String riskSignals;
    private LocalDateTime createdAt;
}
