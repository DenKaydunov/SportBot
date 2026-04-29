package com.github.sportbot.service;

import com.github.sportbot.model.ActivityLevel;
import com.github.sportbot.model.GoalType;
import com.github.sportbot.model.Sex;
import com.github.sportbot.model.WeightChangeSpeed;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class NutritionCalculator {

    private static final float CALORIES_PER_KG = 7700f; // kcal per kg of body fat
    private static final float CALORIES_PER_GRAM_PROTEIN = 4f;
    private static final float CALORIES_PER_GRAM_CARBS = 4f;
    private static final float CALORIES_PER_GRAM_FAT = 9f;
    private static final long MAX_GOAL_DEADLINE_DAYS = 730L;
    private static final float MIN_WEIGHT_DELTA_KG = 2.0f;

    public GoalType determineGoalType(Float currentWeight, Float targetWeight) {
        float diff = targetWeight - currentWeight;
        if (Math.abs(diff) <= MIN_WEIGHT_DELTA_KG) {
            return GoalType.MAINTAIN;
        } else if (diff < 0) {
            return GoalType.LOSS;
        } else {
            return GoalType.GAIN;
        }
    }

    /**
     * Mifflin-St Jeor equation (1990)
     * Men: BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) + 5
     * Women: BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age) - 161
     */
    public float calculateBMR(Sex sex, Integer age, Float weight, Float height) {
        float bmr = (10 * weight) + (6.25f * height) - (5 * age);
        return sex == Sex.MAN ? bmr + 5 : bmr - 161;
    }

    public float calculateTDEE(float bmr, ActivityLevel activityLevel) {
        return switch (activityLevel) {
            case SEDENTARY -> bmr * 1.2f;
            case LIGHT -> bmr * 1.375f;
            case MODERATE -> bmr * 1.55f;
            case ACTIVE -> bmr * 1.725f;
            case VERY_ACTIVE -> bmr * 1.9f;
        };
    }

    public float adjustCaloriesForGoal(float tdee, GoalType goalType, WeightChangeSpeed speed) {
        if (goalType == GoalType.MAINTAIN) {
            return tdee;
        }

        float weeklyCalorieDelta = speed.getKgPerWeek() * CALORIES_PER_KG;
        float dailyCalorieDelta = weeklyCalorieDelta / 7;

        return goalType == GoalType.LOSS ? tdee - dailyCalorieDelta : tdee + dailyCalorieDelta;
    }

    public MacroTargets calculateMacroTargets(float dailyCalories, GoalType goalType) {
        float proteinPercent;
        float carbsPercent;
        float fatPercent;

        switch (goalType) {
            case LOSS -> {
                proteinPercent = 0.40f;
                carbsPercent = 0.30f;
                fatPercent = 0.30f;
            }
            case GAIN -> {
                proteinPercent = 0.35f;
                carbsPercent = 0.45f;
                fatPercent = 0.20f;
            }
            default -> { // MAINTAIN
                proteinPercent = 0.30f;
                carbsPercent = 0.40f;
                fatPercent = 0.30f;
            }
        }

        float proteinGrams = (dailyCalories * proteinPercent) / CALORIES_PER_GRAM_PROTEIN;
        float carbsGrams = (dailyCalories * carbsPercent) / CALORIES_PER_GRAM_CARBS;
        float fatGrams = (dailyCalories * fatPercent) / CALORIES_PER_GRAM_FAT;

        return new MacroTargets(proteinGrams, carbsGrams, fatGrams);
    }

    public LocalDate calculateGoalDeadline(Float currentWeight, Float targetWeight, WeightChangeSpeed speed) {
        float weightDelta = Math.abs(targetWeight - currentWeight);

        // If weight delta is negligible (maintenance goal), return today
        if (weightDelta < 0.1f) {
            return LocalDate.now();
        }

        float weeks = weightDelta / speed.getKgPerWeek();
        long days = Math.round(weeks * 7);

        // Cap maximum goal deadline
        if (days > MAX_GOAL_DEADLINE_DAYS) {
            days = MAX_GOAL_DEADLINE_DAYS;
        }

        return LocalDate.now().plusDays(days);
    }

    public float calculateCaloriesFromMacros(float protein, float carbs, float fat) {
        return (protein * CALORIES_PER_GRAM_PROTEIN)
             + (carbs * CALORIES_PER_GRAM_CARBS)
             + (fat * CALORIES_PER_GRAM_FAT);
    }

    public int calculateCaloriePercentage(float consumed, float target) {
        return target > 0 ? Math.round((consumed / target) * 100) : 0;
    }

    public record MacroTargets(float protein, float carbs, float fat) {}
}
