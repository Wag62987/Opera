package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "http://localhost:5173")
@Slf4j
@RestController
@RequestMapping("/repos")
@RequiredArgsConstructor
public class RepoController {

    private final GithubService githubService;

    // 🔥 GET IMPORTED REPOS
    @GetMapping("/imported")
    public ResponseEntity<?> getAllImportedRepos(
            @AuthenticationPrincipal AppUser appuser) {

        if (appuser == null) {
            return ResponseEntity
                    .status(401)
                    .body("User not authenticated");
        }

        try {
            String githubId = appuser.getGithubId();
            List<GitRepoEntity> repos =
                    githubService.getImportedRepos(githubId);

            return ResponseEntity.ok(repos);

        } catch (Exception e) {
            log.error("Error fetching imported repos", e);
            return ResponseEntity
                    .status(500)
                    .body("Failed to fetch repositories");
        }
    }

    // 🔥 IMPORT REPO
    @PostMapping("/import")
    public ResponseEntity<?> importRepo(
            @AuthenticationPrincipal AppUser appuser,
            @RequestBody GitRepo repo) {
 System.out.println("hit import endpoint with repo: " + repo.getName());
        if (appuser == null) {
            return ResponseEntity
                    .status(401)
                    .body("User not authenticated");
        }

        if (repo == null || repo.getId() == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Invalid repository data");
        }

        try {
            String githubId = appuser.getGithubId();

            GitRepoEntity saved =
                    githubService.importRepo(githubId, repo);

            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            log.error("Error importing repo", e);
            return ResponseEntity
                    .status(500)
                    .body("Failed to import repository");
        }
    }
    @DeleteMapping("/{repoId}")
    public ResponseEntity<GitRepoEntity> DeleteRepo(@PathVariable String repoId){
        GitRepoEntity repo=githubService.DeleteRepo(repoId);
     return ResponseEntity.ok(repo);
    }
   @DeleteMapping("/deleteAll")
public ResponseEntity<String> DeleteALLRepo(@AuthenticationPrincipal AppUser appuser){
   if(githubService.DeleteAllRepo(appuser)){
        return ResponseEntity.ok("success");   
   }
   return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Failed");
}
}