package com.github.sportbot.controller;

import com.github.sportbot.constants.MessageConstants;
import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseService;
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

    @PostMapping
    public String saveExerciseResult(@RequestBody @Valid ExerciseEntryRequest req) {
        exerciseService.saveExerciseResult(req);
        return MessageConstants.EXERCISE_RECORDED;
    }

    @PostMapping("/max")
    public String saveMax(@RequestBody @Valid ExerciseEntryRequest req) {
        return exerciseService.saveExerciseMaxResult(req);
    }
}
