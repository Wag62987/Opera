package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.CICDConfigEntity;

public interface CICDConfigRepository extends JpaRepository<CICDConfigEntity, Long> {

    Optional<CICDConfigEntity> findByRepoId(Long repoId);
}

