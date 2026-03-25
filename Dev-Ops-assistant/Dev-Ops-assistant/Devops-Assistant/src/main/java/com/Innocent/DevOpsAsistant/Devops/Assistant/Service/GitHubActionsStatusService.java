package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.CIStatusResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GitHubActionsStatusService {
    private final AppUserService appUserService;

    private final WebClient githubClient;

   

    /**
     * Fetch latest CI status of a repository
     * @return success | failure | in_progress | cancelled | queued
     */
   public CIStatusResponse fetchLatestCIStatus(
        String githubId,
        GitRepoEntity repo
) {
    AppUser user = appUserService.FindById(githubId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    Map<String, Object> response = githubClient.get()
        .uri("/repos/{owner}/{repo}/actions/runs?per_page=1",
                extractOwner(repo.getRepoUrl()),
                repo.getRepoName())
        .header("Authorization", "Bearer " + user.getGithub_token())
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (response == null || !response.containsKey("workflow_runs")) {
        return new CIStatusResponse("NO_RUNS", null, "No CI data found", null);
    }

    List<Map<String, Object>> runs =
            (List<Map<String, Object>>) response.get("workflow_runs");

    if (runs == null || runs.isEmpty()) {
        return new CIStatusResponse(
                "NO_RUNS",
                null,
                "No CI workflow has run yet",
                null
        );
    }

    Map<String, Object> run = runs.get(0);

    String conclusion = (String) run.get("conclusion");
    String status = (String) run.get("status");
    String logsUrl = (String) run.get("logs_url");

    // Handle running state
    if (conclusion == null) {
        return new CIStatusResponse(
                status.toUpperCase(), // IN_PROGRESS / QUEUED
                null,
                "Workflow is still running",
                logsUrl
        );
    }

    if ("success".equals(conclusion)) {
        return new CIStatusResponse("SUCCESS", null, null, logsUrl);
    }

    return fetchFailureDetails(repo, user.getGithub_token(), run, logsUrl);
}

private CIStatusResponse fetchFailureDetails(
        GitRepoEntity repo,
        String token,
        Map<String, Object> run,
        String logsUrl
) {
    Map<String, Object> jobsResponse = githubClient.get()
        .uri("/repos/{owner}/{repo}/actions/runs/{runId}/jobs",
                extractOwner(repo.getRepoUrl()),
                repo.getRepoName(),
                run.get("id"))
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .bodyToMono(Map.class)
        .block();

    if (jobsResponse == null || !jobsResponse.containsKey("jobs")) {
        return new CIStatusResponse("FAILED", "Unknown", "No job data", logsUrl);
    }

    List<Map<String, Object>> jobs =
            (List<Map<String, Object>>) jobsResponse.get("jobs");

    for (Map<String, Object> job : jobs) {
        List<Map<String, Object>> steps =
                (List<Map<String, Object>>) job.get("steps");

        if (steps == null) continue;

        for (Map<String, Object> step : steps) {
            if ("failure".equals(step.get("conclusion"))) {
                return new CIStatusResponse(
                        "FAILED",
                        (String) step.get("name"),
                        "Step failed during execution",
                        logsUrl
                );
            }
        }
    }

    return new CIStatusResponse(
            "FAILED",
            "Unknown",
            "Pipeline failed, check logs",
            logsUrl
    );
}

    private String extractOwner(String repoUrl) {
        return repoUrl.split("/")[3];
    }
}

