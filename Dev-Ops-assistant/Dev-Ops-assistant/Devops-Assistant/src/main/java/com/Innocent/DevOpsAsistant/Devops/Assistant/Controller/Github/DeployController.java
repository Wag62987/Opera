package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CICDconfigDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GitHubActionsStatusService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubCommitService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubWorkflowService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/deploy")
public class DeployController {

    private final GithubWorkflowService workflowService;
    private final GithubCommitService commitService;
    private final GitHubActionsStatusService statusService;
    private final GithubService githubService;

 @PostMapping("/{repoId}")
  public ResponseEntity<Map<String, Object>> deployRepo(
        @PathVariable String repoId,
        @Valid @RequestBody CICDconfigDTO configDTO,
        @AuthenticationPrincipal AppUser appuser
) {
    CICDConfigEntity config = new CICDConfigEntity();
    config.setProjectType(configDTO.getProjectType());
    config.setBuildTool(configDTO.getBuildTool());
    config.setRuntimeVersion(configDTO.getRuntimeVersion());
    config.setBranchName(configDTO.getBranchName());
    config.setDockerEnabled(configDTO.isDockerEnabled());
    config.setCdEnabled(configDTO.isCdEnabled());
    config.setDeployHookUrl(configDTO.getDeployHookUrl());

    GitRepoEntity repo = githubService.getRepoById(repoId);

    // Generate workflow
    String workflowContent = workflowService.generateWorkflow(config);
 
    // Commit workflow
    commitService.commitWorkflow(appuser.getGithubId(), repo, workflowContent);

    // Fetch CI/CD status
    CIStatusResponse ciStatus = statusService.fetchLatestCIStatus(appuser.getGithubId(), repo);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "CI/CD pipeline triggered");
                response.put("repository", repo.getRepoName());
                response.put("branch", config.getBranchName());
                response.put("ciStatus", ciStatus.getStatus());
                response.put("failedStep", ciStatus.getFailedStep());
                response.put("reason", ciStatus.getReason());
                response.put("logsUrl", ciStatus.getLogsUrl());
                response.put("monitoring", "/ci-status/" + repoId);
        return ResponseEntity.ok(response);
}
}

