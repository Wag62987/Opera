package com.Innocent.DevOpsAsistant.Devops.Assistant.Models;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="appuser")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String github_token;
    private String username;
    private String name;
    @Column(nullable = true)
    private String email;
@Column(name = "github_id", unique = true, nullable = false)
private String githubId;
        @Column(nullable = true)

    private String password;
    private int OTP;
    private long OTPexpier;
   //  ONE AppUser → MANY Repos
    @OneToMany(mappedBy = "appUser", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore 
    private List<GitRepoEntity> repos = new ArrayList<>();
    
    public AppUser(String githubId, String name, String username,String token) {
        this.githubId = githubId;
        this.name = name;
        this.username = username;
        this.github_token = token;
    }

   
    
  

}
