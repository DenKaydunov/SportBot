package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ExerciseEntryRequest(
        @NotNull Long telegramId,
        @NotBlank String exerciseType,
        @NotNull int count
) {}
