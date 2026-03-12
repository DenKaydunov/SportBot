package com.github.sportbot.model;

public record UserExerciseSummary(
        User user,
        ExerciseType exerciseType,
        Long total
) {
}
