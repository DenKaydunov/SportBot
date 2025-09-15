package com.github.sportbot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/squats")
public class SquatsController {


    @GetMapping("/test")
    public String test() {
        return "SquatsController is working!";
    }
}
