package com.github.sportbot.dto;

import com.github.sportbot.model.ExerciseType;

public record WorkoutEvent(
        Integer ownerUserId,
        Integer followerId,
        ExerciseType exerciseType,
        int count
) {}
