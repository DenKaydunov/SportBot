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
     * Returns a random motivation message from DB for specified exercise type and locale.
     *
     * @param exerciseType exercise code (push_up, pull_up, squat, abs)
     * @param locale optional locale (defaults to 'ru' if not provided)
     * @return localized message text
     */
    @GetMapping
    public String getMotivation(
            @RequestParam String exerciseType,
            @RequestParam(defaultValue = "ru") String locale) {
        return motivationService.getMotivation(exerciseType, locale);
    }
}

