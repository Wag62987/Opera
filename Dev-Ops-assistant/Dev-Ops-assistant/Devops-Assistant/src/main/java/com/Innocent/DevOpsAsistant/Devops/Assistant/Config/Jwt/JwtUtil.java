package com.Innocent.DevOpsAsistant.Devops.Assistant.Config.Jwt;

import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.Innocent.DevOpsAsistant.Devops.Assistant.Models.AppUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class JwtUtil {
    @Value("${jwt.secret}")
    private String SECRET;

    private final long EXPIRATION = 86400000; // 1 day

    public String generateToken(AppUser user) {

        return Jwts.builder()
                .setSubject(user.getGithubId()) 
                .claim("username", user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(EXPIRATION + System.currentTimeMillis()))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()),SignatureAlgorithm.HS256)
                .compact();
    }

    public String getGithubId(String token) {
        return getClaims(token).getSubject();
    }

    public String getGithubUsername(String token) {
        return getClaims(token).get("username", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
