package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MotivationEntryRequest(
        @NotBlank String exerciseType
) {}
