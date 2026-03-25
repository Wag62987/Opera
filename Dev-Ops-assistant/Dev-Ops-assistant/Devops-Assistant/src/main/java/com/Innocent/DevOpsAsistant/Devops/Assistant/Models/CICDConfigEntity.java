package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cicd_config")
@Getter
@Setter
@NoArgsConstructor
public class CICDConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectType;
    private String buildTool;
    private String runtimeVersion;
    private String branchName;
    private boolean dockerEnabled;
    private boolean cdEnabled;
    private String deployHookUrl;
    



    @OneToOne
    @JoinColumn(name = "repo_id", nullable = false)
    private GitRepoEntity repo;
}

