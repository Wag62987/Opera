package com.Innocent.DevOpsAsistant.Devops.Assistant.Controller.Authentication;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class Auth {
    @GetMapping("/")
    public String Start(){
        return "<h1>Hello</h1>";
    }
}
