package com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class GitRepo {
    private String id;
    private String name;
    private String description;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("clone_url")
    private String cloneUrl;
    @JsonProperty("stargazers_count")
    private String star;
    private String langauage;

}
