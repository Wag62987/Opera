package com.Innocent.DevOpsAsistant.Devops.Assistant.Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
// import tools.jackson.databind.ObjectMapper;
@Component
public class AuthenticationExceptionHandler implements AuthenticationEntryPoint{

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        // TODO Auto-generated method stub
          response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("authentication", false);
        body.put("message", "Access Denied");

        // ObjectMapper mapper = new ObjectMapper();
        // mapper.writeValue(response.getOutputStream(), body);
    }
    
}
