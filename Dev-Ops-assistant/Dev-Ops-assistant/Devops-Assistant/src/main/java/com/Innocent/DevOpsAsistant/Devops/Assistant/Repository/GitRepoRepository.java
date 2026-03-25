package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.GitRepoEntity;

@Repository
public interface GitRepoRepository extends JpaRepository<GitRepoEntity, Long> {

    boolean existsByGithubRepoId(String githubRepoId);

    List<GitRepoEntity> findByAppUser_GithubId(String githubId);

    public Optional<GitRepoEntity> findByGithubRepoId(String repoId);
}

