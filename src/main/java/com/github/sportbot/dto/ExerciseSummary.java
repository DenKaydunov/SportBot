package com.github.sportbot.dto;

import java.time.LocalDate;

public record ExerciseSummary(
        LocalDate date,
        String exerciseType,
        Integer totalCount
) {}