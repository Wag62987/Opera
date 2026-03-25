package com.Innocent.DevOpsAsistant.Devops.Assistant.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.GitRepo;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Exception.UserNotFound;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Repository.GitRepoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class GithubService {
    private final AppUserService appUserService;
    private final GitRepoRepository gitRepoRepository;
    private final WebClient githubClient;
   // Fetching user's repositories from GitHub 
    public List<GitRepo> getUserRepos(String githubId) throws UserNotFound {
        Optional<AppUser> existingUser = appUserService.FindById(githubId);
        if (existingUser.isPresent()) {
            AppUser existingUserDetails = existingUser.get();
            String Token = existingUserDetails.getGithub_token();
            log.info("Fetching repositories for user: {}", githubId);
            return githubClient
                .get()
                .uri("/user/repos")
                .headers(headers -> headers.setBearerAuth(Token))
                .retrieve()
                .bodyToFlux(GitRepo.class)
                .collectList()
                .block();
        }else{
            log.error("User with GitHub ID {} not found in the system", githubId);
            throw new UserNotFound("User does not exist");
            
        }
    }
        // Importing a repository for a user
      public GitRepoEntity importRepo(String githubId, GitRepo repoDto) {

        AppUser user = appUserService.FindById(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (gitRepoRepository.existsByGithubRepoId(repoDto.getId())) {
            throw new RuntimeException("Repository already imported");
        }

        GitRepoEntity entity = new GitRepoEntity();
        entity.setGithubRepoId(repoDto.getId().toString());
        entity.setRepoName(repoDto.getName());
        entity.setRepoUrl(repoDto.getHtmlUrl());
        entity.setDescription(repoDto.getDescription());
        entity.setLanguage(repoDto.getLangauage());
        entity.setAppUser(user);
        log.info("Importing repository {} for user {}", repoDto.getName(), githubId);
        return gitRepoRepository.save(entity);
    }

    public List<GitRepoEntity> getImportedRepos(String githubId) {
        AppUser user = appUserService.FindById(githubId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        log.info("Fetching imported repositories for user {}", githubId);
        List<GitRepoEntity> repos = user.getRepos();
        return repos;
    }

    public GitRepoEntity getRepoById(String repoId) {
        Optional<GitRepoEntity> repo = gitRepoRepository.findByGithubRepoId(repoId);
                if(repo.isEmpty()){
                    throw new RuntimeException("Repository not found");
                }
        return repo.get();
    }
    public GitRepoEntity DeleteRepo(String repoId){

         Optional<GitRepoEntity> repo = gitRepoRepository.findByGithubRepoId(repoId);
                if(repo.isEmpty()){
                    throw new RuntimeException("Repository not found");
                }
                GitRepoEntity deletedRepo=repo.get();
                gitRepoRepository.delete(deletedRepo);
                
        return deletedRepo;
    }
    public Boolean DeleteAllRepo(AppUser appuser) {
        // TODO Auto-generated method stub
      List<GitRepoEntity> Allrepo=appuser.getRepos();

      for( GitRepoEntity repo : Allrepo){
            gitRepoRepository.delete(repo);
      }
      return true;
    }
}
