package com.github.sportbot.dto;

import jakarta.validation.constraints.NotNull;

public record UserRegistrationResponse(
        @NotNull String responseMessage,
        Integer telegramId,
        String fullName
) {}
