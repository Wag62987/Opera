package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GitHubActionsStatusService;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ci-status")
@RequiredArgsConstructor
public class CIStatusController {

    private final GithubService githubService;
    private final GitHubActionsStatusService statusService;


   
    @GetMapping("/{repoId}")
public ResponseEntity<CIStatusResponse> getStatus(
        @PathVariable String repoId,
        @AuthenticationPrincipal AppUser appuser) {

    GitRepoEntity repo = githubService.getRepoById(repoId);

    CIStatusResponse response =
            statusService.fetchLatestCIStatus(
                    appuser.getGithubId(),
                    repo
            );

    return ResponseEntity.ok(response);
}
}
