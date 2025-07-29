package com.github.sportbot.controller;

import com.github.sportbot.dto.UpdateProgramRequest;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.service.program.UserProgramFetchService;
import com.github.sportbot.service.program.UserProgramUpdateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/programs")
@RequiredArgsConstructor
public class UserProgramController {

    private static final Logger log = LoggerFactory.getLogger(UserProgramController.class);
    private final UserProgramFetchService userProgramFetchService;
    private final UserProgramUpdateService userProgramUpdateService;

    @GetMapping
    public WorkoutPlanResponse getWorkoutPlan(
            @RequestParam Integer telegramId,
            @RequestParam String exerciseType) {
        return userProgramFetchService.getWorkoutPlan(telegramId, exerciseType);
    }

    @PutMapping
    public void updateProgram(@RequestBody @Valid UpdateProgramRequest request) {
        log.info("Received update request: telegramId={}, exerciseType={}",
                request.telegramId(), request.exerciseType());

        userProgramUpdateService.updateProgram(
                request.telegramId(),
                request.exerciseType()
        );
    }
}
