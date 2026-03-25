package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GithubCommitService {
    private final AppUserService appUserService;
    private final WebClient webClient;

    public void commitWorkflow(
            String githubId,
            GitRepoEntity repo,
            String workflowContent
    ) {
        System.out.println(githubId);
         AppUser user = appUserService.FindById(githubId)
            .orElseThrow(() -> new RuntimeException("User not found"));

    String accessToken = user.getGithub_token();
    String path = ".github/workflows/ci.yml";

    String encodedContent = Base64.getEncoder()
            .encodeToString(workflowContent.getBytes(StandardCharsets.UTF_8));

    //fetch sha of file if file alerady exist else store anull
    String sha = webClient.get()
            .uri("/repos/{owner}/{repo}/contents/{path}",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName(),
                    path)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String) response.get("sha"))
            .onErrorResume(e -> Mono.empty()) 
            .block();

    Map<String, Object> body = new HashMap<>();
    body.put("message", sha == null ? "Add CI pipeline" : "Update CI pipeline");
    body.put("content", encodedContent);

    if (sha != null) {
        body.put("sha", sha); 
    }

    
    webClient.put()
            .uri("/repos/{owner}/{repo}/contents/{path}",
                    extractOwner(repo.getRepoUrl()),
                    repo.getRepoName(),
                    path)
            .header("Authorization", "Bearer " + accessToken)
            .bodyValue(body)
            .retrieve()
            .toBodilessEntity()
            .block();
    }

    private String extractOwner(String repoUrl) {
        return repoUrl.split("/")[3];
    }
}

