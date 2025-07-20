package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @PostMapping
    public void saveEntry(@RequestBody @Valid ExerciseEntryRequest req) {
        exerciseService.saveExerciseEntry(req);
    }
}
