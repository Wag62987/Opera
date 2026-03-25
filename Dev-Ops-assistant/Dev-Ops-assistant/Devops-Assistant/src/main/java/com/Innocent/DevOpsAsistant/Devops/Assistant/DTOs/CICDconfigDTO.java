package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;       
import lombok.NoArgsConstructor;
import lombok.Setter;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter     
public class CICDconfigDTO {
    @NotBlank(message = "Project type is required")
    private String projectType;
    @NotBlank(message = "Build tool is required")
    private String buildTool;
    @NotBlank(message = "Runtime version is required")
    private String runtimeVersion;
    @NotBlank(message = "Branch name is required")
    private String branchName;
    @NotNull(message = "Docker enabled flag is required")
    private boolean dockerEnabled;
    @NotNull(message = "cd enabled flag is required")
    private boolean cdEnabled;
    @NotBlank(message="Bulid hook Url is needed")
    private String deployHookUrl;
    
  
}   