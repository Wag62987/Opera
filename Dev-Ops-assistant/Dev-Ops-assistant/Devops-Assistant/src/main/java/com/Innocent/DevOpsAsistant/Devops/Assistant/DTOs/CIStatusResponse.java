package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CIStatusResponse {
    private String status;
    private String failedStep;
    private String reason;
    private String logsUrl;
}
