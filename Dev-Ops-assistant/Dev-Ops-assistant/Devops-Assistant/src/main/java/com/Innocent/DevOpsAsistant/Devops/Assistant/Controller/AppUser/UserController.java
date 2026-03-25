package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.AppUser;

import org.springframework.web.bind.annotation.RestController;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Config.Jwt.JwtUtil;
import com.Innocent.DevOpsAsistant.Devops.Assistant.DTOs.UserDTO;
import com.Innocent.DevOpsAsistant.Devops.Assistant.Service.AppUserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
 private final AppUserService userService;
 private final JwtUtil jwtUtil;

 @GetMapping("/Info")
public ResponseEntity<UserDTO> getUserInfo(
        @CookieValue(name = "JWT_TOKEN", required = false) String jwtToken,
        @RequestHeader(name = "Authorization", required = false) String authHeader
) {
    if (jwtToken == null && authHeader != null && authHeader.startsWith("Bearer ")) {
        jwtToken = authHeader.substring(7);
    }

    if (jwtToken == null) {
        return ResponseEntity.status(401).build();
    }

    String githubId = jwtUtil.getGithubId(jwtToken);
    if (githubId == null) {
        return ResponseEntity.status(401).build();
    }

    UserDTO user = userService.GetUserInfo(userService.FindById(githubId));
    return ResponseEntity.ok(user);
}
 }
