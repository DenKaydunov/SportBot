package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseEntryRequest(
        @NotNull Integer telegramId,
        @NotBlank String exerciseType,
        int count
) {}
