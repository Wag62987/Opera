package com.Innocent.DevOpsAsistant.Devops.Assistant.Config;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Config.Jwt.JwtUtil;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppUserService userService;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2AuthenticationToken authToken =
                (OAuth2AuthenticationToken) authentication;

        Map<String, Object> attributes =
                authToken.getPrincipal().getAttributes();

        String githubId = String.valueOf(attributes.get("id"));
        String username = String.valueOf(attributes.get("login"));
        String name = String.valueOf(attributes.get("name"));

        OAuth2AuthorizedClient client =
                authorizedClientService.loadAuthorizedClient(
                        authToken.getAuthorizedClientRegistrationId(),
                        authToken.getName()
                );

        String githubAccessToken =
                client.getAccessToken().getTokenValue();

        Optional<AppUser> existingUser =
                userService.FindById(githubId);

        AppUser user;

        if (existingUser.isEmpty()) {
            user = new AppUser();
            user.setGithubId(githubId);
            user.setUsername(username);
            user.setName(name);
            user.setGithub_token(githubAccessToken);
        } else {
            user = existingUser.get();
            user.setGithub_token(githubAccessToken);
        }

        userService.Save(user);

        String jwtToken = jwtUtil.generateToken(user);
        // log.info("Generated JWT Token for user {}: {}", username, jwtToken);
        Cookie cookie = new Cookie("JWT_TOKEN", jwtToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); 
        cookie.setPath("/");     
        cookie.setMaxAge(24 * 60 * 60); // 1 day
        response.addCookie(cookie);
        response.setHeader("Authorization", "Bearer " + jwtToken);
        log.info("User {} authenticated successfully. JWT token set in cookie and header.", username);
        response.sendRedirect("http://localhost:5173/dashboard");

    }
}
