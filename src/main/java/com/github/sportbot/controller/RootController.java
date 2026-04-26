package com.github.sportbot.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @Value("${bot.name}")
    private String botName;

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("service", "SportBot API");
        response.put("status", "running");
        response.put("bot", "@" + botName);
        response.put("documentation", "/swagger-ui/index.html");

        Map<String, String> endpoints = new LinkedHashMap<>();
        endpoints.put("API Base", "/api/v1");
        endpoints.put("Exercises", "/api/v1/exercises");
        endpoints.put("Users", "/api/v1/users");
        endpoints.put("Leaderboard", "/api/v1/leaderboard");
        endpoints.put("Achievements", "/api/v1/achievements");
        endpoints.put("Subscriptions", "/api/v1/subscriptions");
        endpoints.put("Admin", "/admin/achievements");

        response.put("endpoints", endpoints);

        return response;
    }
}
