package com.github.sportbot.controller;

import com.github.sportbot.dto.UpdateProgramRequest;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.service.UserProgramService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class UserProgramController {

    private final UserProgramService userProgramService;

    @GetMapping
    public WorkoutPlanResponse getWorkoutPlan(
            @RequestParam @NotNull Long telegramId,
            @RequestParam String exerciseType) {
        return userProgramService.getWorkoutPlan(telegramId, exerciseType);
    }

    @PutMapping
    public void updateProgram(@RequestBody @Valid UpdateProgramRequest request) {
        userProgramService.incrementDayProgram(
                request.telegramId(),
                request.exerciseType()
        );
    }
}
