package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateProgramRequest(
        @NotNull Long telegramId,
        @NotBlank String exerciseType
) {}
