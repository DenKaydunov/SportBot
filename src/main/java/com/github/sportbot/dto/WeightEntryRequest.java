package com.github.sportbot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record WeightEntryRequest(
    @NotNull Long telegramId,
    @NotNull @Positive Float weight,
    LocalDate date  // optional, defaults to today if null
) {}
