package com.github.sportbot.controller;

import com.github.sportbot.service.MotivationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides random motivation message.
 */
@RestController
@RequestMapping("/api/v1/motivation")
@RequiredArgsConstructor
public class MotivationController {

    private final MotivationService motivationService;

    /**
     * Returns a random motivation message from DB.
     *
     * @return message text
     */
    @GetMapping
    public String getMotivation(@RequestParam String exerciseType) {
        return motivationService.getMotivation(exerciseType);
    }
}

