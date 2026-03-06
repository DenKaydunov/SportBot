package com.github.sportbot.dto;

import com.github.sportbot.model.Sex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record RegistrationRequest(
        @NotNull Long telegramId,
        @NotNull String sendPulseId,
        boolean isSubscribed,
        @NotBlank String fullName,
        String language,
        Sex sex,
        Integer age,
        Integer referrerTelegramId,
        LocalTime remindTime
) {}
