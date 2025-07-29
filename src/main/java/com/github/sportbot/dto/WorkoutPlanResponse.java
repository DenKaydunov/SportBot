package com.github.sportbot.dto;

import java.util.List;

public record WorkoutPlanResponse(
        List<Integer> sets, // по 5 значений
        int totalReps,
        String message
) {}