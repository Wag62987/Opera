package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Github;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Exception.UserNotFound;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.GithubService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/github")
@RequiredArgsConstructor
public class githubController {
    private final GithubService githubService;

    @GetMapping("/userRepos")
    public ResponseEntity<List<GitRepo>> getUserRepos(
        @AuthenticationPrincipal AppUser appuser) {

    String githubId = appuser.getGithubId();

        try{

            List<GitRepo> repos=githubService.getUserRepos(githubId);
            
            log.info("Fetched {} repositories for user {}", repos.size(), githubId);
           
            return ResponseEntity.ok(repos);
        }
        catch(UserNotFound e){
            log.error("User not found: {}", githubId);
        return ResponseEntity.badRequest().body(null);
    }
}

}
