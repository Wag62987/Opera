package com.Innocent.DevOpsAsistant.Devops.Assistant.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser,Long> {
     Optional<AppUser> findByGithubId(String githubId);

     Optional<AppUser> findByEmail(String email);
}
