package com.github.sportbot.service;

import com.github.sportbot.model.GoalType;
import com.github.sportbot.model.NutritionProfile;
import com.github.sportbot.repository.MacroProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class NutritionResponseFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final float MIN_CALORIE_THRESHOLD = 0.1f;
    private static final float MIN_WEIGHT_CHANGE_THRESHOLD = 0.1f;
    private static final long MIN_CONSISTENT_MEAL_DAYS = 20L;
    private static final float MIN_WORKOUTS_FOR_PROTEIN_ADVICE = 3f;

    private final MessageLocalizer messageLocalizer;

    public String formatProfileCreated(ProfileCreatedData data, Locale locale) {
        float weightDelta = data.targetWeight() - data.currentWeight();
        String goalTypeLocalized = messageLocalizer.localize("nutrition.goal." + data.goalType().name().toLowerCase(), new Object[]{}, locale);
        String activityLevelLocalized = messageLocalizer.localize("nutrition.activity." + data.activityLevel().name().toLowerCase(), new Object[]{}, locale);

        return messageLocalizer.localize("nutrition.profile.created", new Object[]{
            data.currentWeight(),
            data.targetWeight(),
            Math.abs(weightDelta),
            data.goalDeadline().format(DATE_FORMATTER),
            data.weightChangeSpeed().getKgPerWeek(),
            Math.round(data.dailyCalories()),
            Math.round(data.macros().protein()),
            Math.round(data.macros().fat()),
            Math.round(data.macros().carbs()),
            activityLevelLocalized,
            goalTypeLocalized
        }, locale);
    }

    public String formatMealLogged(MealLoggedData data, Locale locale) {
        if (data.profile() != null) {
            return messageLocalizer.localize("nutrition.meal.logged", new Object[]{
                data.foodName(),
                Math.round(data.calculatedCalories()),
                Math.round(data.protein()),
                Math.round(data.fat()),
                Math.round(data.carbs()),
                Math.round(data.totalCalories()),
                Math.round(data.profile().getDailyCalorieTarget()),
                data.percentage(),
                Math.round(data.macros().getProtein()),
                Math.round(data.profile().getProteinTarget()),
                Math.round(data.macros().getFat()),
                Math.round(data.profile().getFatTarget()),
                Math.round(data.macros().getCarbs()),
                Math.round(data.profile().getCarbsTarget())
            }, locale);
        } else {
            return messageLocalizer.localize("nutrition.meal.logged.no_profile", new Object[]{
                data.foodName(),
                Math.round(data.calculatedCalories()),
                Math.round(data.protein()),
                Math.round(data.fat()),
                Math.round(data.carbs())
            }, locale);
        }
    }

    public String formatDailySummary(
            LocalDate targetDate,
            Float totalCalories,
            MacroProjection macros,
            NutritionProfile profile,
            int percentage,
            Locale locale
    ) {
        if (totalCalories == null || totalCalories < MIN_CALORIE_THRESHOLD || macros == null) {
            return messageLocalizer.localize("nutrition.summary.no_meals", new Object[]{}, locale);
        }

        if (profile != null) {
            return messageLocalizer.localize("nutrition.summary.daily", new Object[]{
                targetDate.format(DATE_FORMATTER),
                Math.round(totalCalories),
                Math.round(profile.getDailyCalorieTarget()),
                percentage,
                Math.round(macros.getProtein()),
                Math.round(profile.getProteinTarget()),
                Math.round(macros.getFat()),
                Math.round(profile.getFatTarget()),
                Math.round(macros.getCarbs()),
                Math.round(profile.getCarbsTarget())
            }, locale);
        } else {
            return messageLocalizer.localize("nutrition.summary.daily.no_profile", new Object[]{
                targetDate.format(DATE_FORMATTER),
                Math.round(totalCalories),
                Math.round(macros.getProtein()),
                Math.round(macros.getFat()),
                Math.round(macros.getCarbs())
            }, locale);
        }
    }

    public String formatWeightLogged(float weight, String changeMessage, Locale locale) {
        return messageLocalizer.localize("nutrition.weight.logged", new Object[]{
            weight,
            changeMessage
        }, locale);
    }

    public String formatWeightChange(float delta, Locale locale) {
        if (Math.abs(delta) < MIN_WEIGHT_CHANGE_THRESHOLD) {
            return messageLocalizer.localize("nutrition.weight.change.same", new Object[]{}, locale);
        } else if (delta < 0) {
            return messageLocalizer.localize("nutrition.weight.change.loss", new Object[]{Math.abs(delta)}, locale);
        } else {
            return messageLocalizer.localize("nutrition.weight.change.gain", new Object[]{delta}, locale);
        }
    }

    public String formatRecommendations(
            GoalType goalType,
            Long mealDays,
            float workoutsPerWeek,
            float proteinTarget,
            Locale locale
    ) {
        String goalTypeLocalized = messageLocalizer.localize("nutrition.goal." + goalType.name().toLowerCase(), new Object[]{}, locale);

        StringBuilder recommendations = new StringBuilder();
        recommendations.append(messageLocalizer.localize("nutrition.rec.header", new Object[]{}, locale)).append("\n");
        recommendations.append(messageLocalizer.localize("nutrition.rec.goal", new Object[]{goalTypeLocalized}, locale)).append("\n");
        recommendations.append(messageLocalizer.localize("nutrition.rec.progress", new Object[]{mealDays}, locale)).append("\n");

        // Consistency advice
        if (mealDays >= MIN_CONSISTENT_MEAL_DAYS) {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.consistent", new Object[]{}, locale)).append("\n");
        } else {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.start", new Object[]{}, locale)).append("\n");
        }

        // Protein advice based on workouts
        if (workoutsPerWeek >= MIN_WORKOUTS_FOR_PROTEIN_ADVICE) {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.protein", new Object[]{
                Math.round(workoutsPerWeek),
                Math.round(proteinTarget)
            }, locale));
        }

        return recommendations.toString();
    }
}
