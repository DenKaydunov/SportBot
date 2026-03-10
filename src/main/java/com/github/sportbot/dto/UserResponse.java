package com.github.sportbot.dto;

import jakarta.validation.constraints.NotNull;

public record UserResponse(
        @NotNull String responseMessage,
        Long telegramId,
        String fullName
) {}
