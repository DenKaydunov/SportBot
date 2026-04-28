package com.github.sportbot.dto;

import com.github.sportbot.model.ActivityLevel;
import com.github.sportbot.model.WeightChangeSpeed;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record NutritionProfileRequest(
    @NotNull Long telegramId,
    @NotNull @Positive Float currentWeight,      // in kg
    @NotNull @Positive Float height,             // in cm
    @NotNull @Positive Float targetWeight,       // in kg
    @NotNull ActivityLevel activityLevel,
    String dietaryRestrictions,                  // optional, can be null or "нет"
    @NotNull WeightChangeSpeed weightChangeSpeed
) {}
