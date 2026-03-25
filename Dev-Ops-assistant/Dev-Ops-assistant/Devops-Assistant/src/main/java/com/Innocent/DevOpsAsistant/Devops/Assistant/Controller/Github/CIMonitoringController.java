package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.WorkflowRunsResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GitHubMonitoringService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ci-monitor")
@RequiredArgsConstructor
public class CIMonitoringController {

    private final GithubService githubService;
    private final GitHubMonitoringService monitoringService;

    @GetMapping("/{repoId}")
    public ResponseEntity<WorkflowRunsResponse> monitor(
        @PathVariable String repoId,
        @AuthenticationPrincipal AppUser user) {

    GitRepoEntity repo = githubService.getRepoById(repoId);

    WorkflowRunsResponse response = monitoringService.getWorkflowRuns(
            user.getUsername(),
            repo,
            user.getGithub_token()
    );
    System.out.println(response.getTotalCount());

    return ResponseEntity.ok(response);
    }
}