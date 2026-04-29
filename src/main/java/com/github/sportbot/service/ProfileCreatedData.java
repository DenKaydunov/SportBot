package com.github.sportbot.service;

import com.github.sportbot.model.ActivityLevel;
import com.github.sportbot.model.GoalType;
import com.github.sportbot.model.WeightChangeSpeed;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record ProfileCreatedData(
    Float currentWeight,
    Float targetWeight,
    LocalDate goalDeadline,
    WeightChangeSpeed weightChangeSpeed,
    float dailyCalories,
    NutritionCalculator.MacroTargets macros,
    ActivityLevel activityLevel,
    GoalType goalType
) {}
