package com.github.sportbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalTime;

public record MealEntryRequest(
    @NotNull Long telegramId,
    @NotBlank String foodName,
    @NotNull @PositiveOrZero Float protein,
    @NotNull @PositiveOrZero Float carbs,
    @NotNull @PositiveOrZero Float fat,
    LocalTime mealTime  // optional, can be null
) {}
