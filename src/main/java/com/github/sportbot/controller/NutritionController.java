package com.github.sportbot.controller;

import com.github.sportbot.dto.MealEntryRequest;
import com.github.sportbot.dto.NutritionProfileRequest;
import com.github.sportbot.dto.WeightEntryRequest;
import com.github.sportbot.service.NutritionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/nutrition")
@RequiredArgsConstructor
@Tag(name = "Nutrition Management", description = "API для управления питанием и расчёта калорий")
public class NutritionController {

    private final NutritionService nutritionService;

    @PostMapping("/profile")
    @Operation(
        summary = "Create or update nutrition profile",
        description = "Creates or updates user's nutrition profile with complete questionnaire data. Calculates daily calorie and macro targets using Mifflin-St Jeor equation."
    )
    public String createOrUpdateProfile(@RequestBody @Valid NutritionProfileRequest request) {
        return nutritionService.createOrUpdateProfile(request);
    }

    @GetMapping("/profile/{telegramId}")
    @Operation(
        summary = "Get user's nutrition profile",
        description = "Returns user's nutrition profile with formatted output including targets and goal deadline"
    )
    public String getProfile(
            @PathVariable
            @Parameter(example = "1000001", description = "User's Telegram ID")
            Long telegramId) {
        // For now just return simple message, can be enhanced with full profile response
        return nutritionService.getRecommendations(telegramId);
    }

    @PostMapping("/meals")
    @Operation(
        summary = "Log a meal or food entry",
        description = "Records a meal with calories and macronutrients (protein, carbs, fat). Returns confirmation with daily totals."
    )
    public String logMeal(@RequestBody @Valid MealEntryRequest request) {
        return nutritionService.logMeal(request);
    }

    @GetMapping("/meals/daily")
    @Operation(
        summary = "Get daily meal summary",
        description = "Returns total calories and macros for a specific date (defaults to today). Compares against targets if profile exists."
    )
    public String getDailySummary(
            @RequestParam
            @Parameter(example = "1000001", description = "User's Telegram ID")
            Long telegramId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(example = "2026-04-28", description = "Date to get summary for (optional, defaults to today)")
            LocalDate date
    ) {
        return nutritionService.getDailySummary(telegramId, date);
    }

    @PostMapping("/weight")
    @Operation(
        summary = "Log weight measurement",
        description = "Records user's weight for a specific date. Updates current weight in profile if logging today's weight."
    )
    public String logWeight(@RequestBody @Valid WeightEntryRequest request) {
        return nutritionService.logWeight(request);
    }

    @GetMapping("/recommendations/{telegramId}")
    @Operation(
        summary = "Get personalized nutrition recommendations",
        description = "Analyzes user's workout patterns, meal logging consistency, and provides personalized nutrition advice"
    )
    public String getRecommendations(
            @PathVariable
            @Parameter(example = "1000001", description = "User's Telegram ID")
            Long telegramId) {
        return nutritionService.getRecommendations(telegramId);
    }
}
