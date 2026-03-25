package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.WorkflowRunDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.WorkflowRunsResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

@Service
public class GitHubMonitoringService {

    private final RestTemplate restTemplate = new RestTemplate();

    public WorkflowRunsResponse getWorkflowRuns(
            String githubUser,
            GitRepoEntity repo,
            String githubToken) {

        String url = "https://api.github.com/repos/"
                + githubUser + "/" + repo.getRepoName()
                + "/actions/runs";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(githubToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        Map body = response.getBody();

        List<Map> runs = (List<Map>) body.get("workflow_runs");

        List<WorkflowRunDTO> result = new ArrayList<>();

        if (runs == null) {
            return new WorkflowRunsResponse(0, result);
        }

        for (Map run : runs) {

            String status = (String) run.get("status");
            String conclusion = (String) run.get("conclusion");

            String finalStatus;

            if ("completed".equals(status) && "success".equals(conclusion)) {
                finalStatus = "SUCCESS";
            } else if ("completed".equals(status) && "failure".equals(conclusion)) {
                finalStatus = "FAILED";
            } else {
                finalStatus = "RUNNING";
            }

            String startedAt = (String) run.get("run_started_at");

            WorkflowRunDTO dto = new WorkflowRunDTO(
                    ((Number) run.get("id")).longValue(),
                    (String) run.get("name"),
                    finalStatus,
                    (String) run.get("head_branch"),
                    (String) run.get("head_sha"),
                    startedAt
            );

            result.add(dto);
        }

        int totalCount = (int) body.getOrDefault("total_count", result.size());

        return new WorkflowRunsResponse(totalCount, result);
    }
}