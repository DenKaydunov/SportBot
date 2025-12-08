package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExerciseEntryRequest(
        @NotNull Long telegramId,
        @NotBlank String exerciseType,
        @Positive int count
) {}
