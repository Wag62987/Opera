package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  
     @Bean
    public WebClient githubClient() {
        return WebClient.builder()
              .baseUrl("https://api.github.com")
              .defaultHeader("Accept", "application/vnd.github+json")
              .build();
    }
}
