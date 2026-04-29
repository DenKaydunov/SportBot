package com.github.sportbot.service;

import com.github.sportbot.model.NutritionProfile;
import com.github.sportbot.repository.MacroProjection;
import lombok.Builder;

@Builder
public record MealLoggedData(
    String foodName,
    float calculatedCalories,
    float protein,
    float fat,
    float carbs,
    Float totalCalories,
    MacroProjection macros,
    NutritionProfile profile,
    int percentage
) {}
