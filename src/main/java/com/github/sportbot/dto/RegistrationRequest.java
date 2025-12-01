package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RegistrationRequest(
        @NotNull Long telegramId,
        @NotNull String sendPulseId,
        boolean isSubscribed,
        @NotBlank String fullName,
        Integer referrerTelegramId,
        LocalTime remindTime
) {}
