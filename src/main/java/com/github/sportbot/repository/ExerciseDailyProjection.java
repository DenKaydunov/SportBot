package com.github.sportbot.repository;

import java.time.LocalDate;

public interface ExerciseDailyProjection {
    LocalDate getDate();
    String getExerciseType();
    Integer getTotalCount();
}
