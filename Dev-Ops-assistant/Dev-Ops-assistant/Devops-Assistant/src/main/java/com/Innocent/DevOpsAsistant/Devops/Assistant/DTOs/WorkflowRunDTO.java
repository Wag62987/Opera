package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class WorkflowRunDTO {

    private Long id;
    private String name;
    private String status;
    private String branch;
    private String commit;
    private String date;
    
}