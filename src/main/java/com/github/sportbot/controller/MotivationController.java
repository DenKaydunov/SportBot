package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.dto.MotivationEntryRequest;
import com.github.sportbot.dto.MotivationResponse;
import com.github.sportbot.service.MotivationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Provides random motivation message.
 */
@RestController
@RequestMapping("/api/v1/motivation")
@RequiredArgsConstructor
public class MotivationController {

    private final MotivationService motivationService;

    /**
     * Returns a motivation message from DB.
     * @return message text
     */
    @GetMapping
    public MotivationResponse getMotivation(
            @RequestParam String exerciseType
    ) {
        return motivationService.getMotivation(exerciseType);
    }
}

