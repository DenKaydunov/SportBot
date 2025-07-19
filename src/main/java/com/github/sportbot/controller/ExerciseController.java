package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    @Autowired
    private ExerciseService exerciseService;

    @PostMapping
    public ResponseEntity<Void> saveEntry(@RequestBody @Valid ExerciseEntryRequest req) {
        exerciseService.saveExerciseEntry(req);
        return ResponseEntity.ok().build();
    }
}
