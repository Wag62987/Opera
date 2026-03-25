package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.WorkflowRunDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.WorkflowRunsResponse;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GitHubMonitoringService Tests")
class GitHubMonitoringServiceTest {

    private GitHubMonitoringService monitoringService;
    private GitRepoEntity mockRepo;

    @BeforeEach
    void setUp() {
        monitoringService = new GitHubMonitoringService();

        mockRepo = new GitRepoEntity();
        mockRepo.setRepoName("my-repo");
        mockRepo.setRepoUrl("https://github.com/john_doe/my-repo");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Map<String, Object> buildRun(String status, String conclusion,
                                         String headSha, String headBranch, String startedAt) {
        Map<String, Object> run = new HashMap<>();
        run.put("status", status);
        run.put("conclusion", conclusion);
        run.put("head_sha", headSha);
        run.put("head_branch", headBranch);
        run.put("run_started_at", startedAt);
        run.put("id", 1L);          // required by WorkflowRunDTO
        run.put("name", "workflow"); // required by WorkflowRunDTO
        return run;
    }

    private RestTemplate mockRestTemplate(Map<String, Object> body) {
        RestTemplate rt = mock(RestTemplate.class);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(body, HttpStatus.OK);
        when(rt.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);
        return rt;
    }

    private void injectRestTemplate(GitHubMonitoringService service, RestTemplate rt) throws Exception {
        var field = GitHubMonitoringService.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(service, rt);
    }

    // ─── Tests ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("completed+success run should map to SUCCESS")
    void getWorkflowRuns_successRun_shouldMapToSuccess() throws Exception {
        Map<String, Object> run = buildRun("completed", "success", "abc123", "main", "2024-01-01T10:00:00Z");
        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));

        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        List<WorkflowRunDTO> runs = response.getWorkflowRuns();

        assertThat(runs).hasSize(1);
        assertThat(runs.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(runs.get(0).getBranch()).isEqualTo("main");
        assertThat(runs.get(0).getCommit()).isEqualTo("abc123");
    }

    @Test
    @DisplayName("completed+failure run should map to FAILED")
    void getWorkflowRuns_failedRun_shouldMapToFailed() throws Exception {
        Map<String, Object> run = buildRun("completed", "failure", "def456", "develop", "2024-01-02T11:00:00Z");
        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));

        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        WorkflowRunDTO result = response.getWorkflowRuns().get(0);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getBranch()).isEqualTo("develop");
        assertThat(result.getCommit()).isEqualTo("def456");
    }

    @Test
    @DisplayName("in_progress run should map to RUNNING")
    void getWorkflowRuns_inProgressRun_shouldMapToRunning() throws Exception {
        Map<String, Object> run = buildRun("in_progress", null, "ghi789", "feature/x", "2024-01-03T12:00:00Z");
        Map<String, Object> apiBody = Map.of("workflow_runs", List.of(run));

        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        WorkflowRunDTO result = response.getWorkflowRuns().get(0);

        assertThat(result.getStatus()).isEqualTo("RUNNING");
        assertThat(result.getBranch()).isEqualTo("feature/x");
        assertThat(result.getCommit()).isEqualTo("ghi789");
    }

    @Test
    @DisplayName("should return empty list when workflow_runs is null")
    void getWorkflowRuns_nullRuns_shouldReturnEmptyList() throws Exception {
        Map<String, Object> apiBody = new HashMap<>();
        apiBody.put("workflow_runs", null);

        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        assertThat(response.getWorkflowRuns()).isEmpty();
    }

    @Test
    @DisplayName("should return empty list when workflow_runs is empty")
    void getWorkflowRuns_emptyRuns_shouldReturnEmptyList() throws Exception {
        Map<String, Object> apiBody = Map.of("workflow_runs", List.of());

        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        assertThat(response.getWorkflowRuns()).isEmpty();
    }

    @Test
    @DisplayName("should process multiple runs correctly")
    void getWorkflowRuns_multipleRuns_shouldReturnAll() throws Exception {
        List<Map<String, Object>> runs = List.of(
                buildRun("completed", "success", "sha1", "main", "2024-01-01T00:00:00Z"),
                buildRun("completed", "failure", "sha2", "feature/x", "2024-01-02T00:00:00Z"),
                buildRun("in_progress", null, "sha3", "hotfix/y", "2024-01-03T00:00:00Z")
        );

        Map<String, Object> apiBody = Map.of("workflow_runs", runs);
        injectRestTemplate(monitoringService, mockRestTemplate(apiBody));

        WorkflowRunsResponse response = monitoringService.getWorkflowRuns("john_doe", mockRepo, "token");
        List<WorkflowRunDTO> result = response.getWorkflowRuns();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getStatus()).isEqualTo("SUCCESS");
        assertThat(result.get(1).getStatus()).isEqualTo("FAILED");
        assertThat(result.get(2).getStatus()).isEqualTo("RUNNING");
    }
}