package com.github.sportbot.controller;

import com.github.sportbot.constants.MessageConstants;
import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseService;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercises")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @Autowired
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @PostMapping
    public String saveEntry(@RequestBody @Valid ExerciseEntryRequest req) {
        exerciseService.saveExerciseEntry(req);
        return MessageConstants.EXERCISE_RECORDED;
    }

    @PostMapping("/max")
    public String saveMax(@RequestBody @Valid ExerciseEntryRequest req) {
        exerciseService.saveMaxEntry(req);
        return MessageConstants.MAX_EXERCISE_RECORDED;
    }
}
