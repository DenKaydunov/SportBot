package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseService;
import com.github.sportbot.service.UserMaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserMaxService userMaxService;

    @PostMapping
    public String saveExerciseResult(@RequestBody @Valid ExerciseEntryRequest req) {
        return exerciseService.saveExerciseResult(req);
    }

    @PostMapping("/max")
    public String saveMax(@RequestBody @Valid ExerciseEntryRequest req) {
        return userMaxService.saveExerciseMaxResult(req);
    }
}
