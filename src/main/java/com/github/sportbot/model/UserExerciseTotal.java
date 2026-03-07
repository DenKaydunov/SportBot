package com.github.sportbot.model;

public record UserExerciseTotal(
        User user,
        ExerciseType exerciseType,
        Long total
) {
}
