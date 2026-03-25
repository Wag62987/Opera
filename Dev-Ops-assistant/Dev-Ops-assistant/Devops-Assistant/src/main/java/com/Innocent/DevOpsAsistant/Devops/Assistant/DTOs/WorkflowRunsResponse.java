package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class WorkflowRunsResponse {

    private int totalCount;
    private List<WorkflowRunDTO> workflowRuns;
}