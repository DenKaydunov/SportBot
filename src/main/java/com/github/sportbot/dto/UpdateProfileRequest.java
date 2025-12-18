package com.github.sportbot.dto;

import com.github.sportbot.model.Sex;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateProfileRequest(
        @NotNull Long telegramId,
        @PositiveOrZero Integer age,
        Sex sex,
        // allow empty or 2-letter code; empty means "clear" / unspecified
        @Pattern(regexp = "^$|^[a-zA-Z]{2}$", message = "Language must be empty or a 2-letter code")
        String language
) {}

