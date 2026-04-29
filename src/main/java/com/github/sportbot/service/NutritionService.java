package com.github.sportbot.service;

import com.github.sportbot.dto.MealEntryRequest;
import com.github.sportbot.dto.NutritionProfileRequest;
import com.github.sportbot.dto.WeightEntryRequest;
import com.github.sportbot.exception.InvalidNutritionDataException;
import com.github.sportbot.exception.NutritionProfileNotFoundException;
import com.github.sportbot.exception.UserDataInsufficientException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private static final float WEEKS_IN_MONTH = 30f / 7f; // ~4.3 weeks
    private static final float MIN_WEIGHT_KG = 25f;
    private static final float MAX_WEIGHT_KG = 300f;
    private static final float MIN_HEIGHT_CM = 100f;
    private static final float MAX_HEIGHT_CM = 250f;
    private static final float MAX_WEIGHT_CHANGE_KG = 100f;
    private static final float MAX_PROTEIN_PER_MEAL_G = 200f;
    private static final float MAX_CARBS_PER_MEAL_G = 500f;
    private static final float MAX_FAT_PER_MEAL_G = 200f;
    private static final float MAX_CALORIES_PER_MEAL = 5000f;
    private static final float MIN_MACRO_VALUE = 0.01f;

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightHistoryRepository weightHistoryRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserService userService;
    private final NutritionCalculator calculator;
    private final NutritionResponseFormatter formatter;

    @Transactional
    public String createOrUpdateProfile(NutritionProfileRequest request) {
        User user = userRepository.findByTelegramId(request.telegramId())
                .orElseThrow(UserNotFoundException::new);

        // Validate user has required data
        if (user.getAge() == null || user.getSex() == null) {
            throw new UserDataInsufficientException("User age and sex are required for calorie calculation");
        }

        // Validate business rules
        validateNutritionProfile(request);

        Locale locale = userService.getUserLocale(user);

        // Calculate all targets
        GoalType goalType = calculator.determineGoalType(request.currentWeight(), request.targetWeight());
        float bmr = calculator.calculateBMR(user.getSex(), user.getAge(), request.currentWeight(), request.height());
        float tdee = calculator.calculateTDEE(bmr, request.activityLevel());
        float dailyCalories = calculator.adjustCaloriesForGoal(tdee, goalType, request.weightChangeSpeed());
        NutritionCalculator.MacroTargets macros = calculator.calculateMacroTargets(dailyCalories, goalType);
        LocalDate goalDeadline = calculator.calculateGoalDeadline(
            request.currentWeight(),
            request.targetWeight(),
            request.weightChangeSpeed()
        );

        // Create or update profile
        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(request.telegramId())
                .orElse(NutritionProfile.builder().user(user).build());

        profile.setCurrentWeight(request.currentWeight());
        profile.setHeight(request.height());
        profile.setTargetWeight(request.targetWeight());
        profile.setActivityLevel(request.activityLevel());
        profile.setDietaryRestrictions(request.dietaryRestrictions());
        profile.setWeightChangeSpeed(request.weightChangeSpeed());
        profile.setGoalType(goalType);
        profile.setDailyCalorieTarget(dailyCalories);
        profile.setProteinTarget(macros.protein());
        profile.setCarbsTarget(macros.carbs());
        profile.setFatTarget(macros.fat());
        profile.setGoalDeadline(goalDeadline);

        nutritionProfileRepository.save(profile);

        ProfileCreatedData data = ProfileCreatedData.builder()
            .currentWeight(request.currentWeight())
            .targetWeight(request.targetWeight())
            .goalDeadline(goalDeadline)
            .weightChangeSpeed(request.weightChangeSpeed())
            .dailyCalories(dailyCalories)
            .macros(macros)
            .activityLevel(request.activityLevel())
            .goalType(goalType)
            .build();

        return formatter.formatProfileCreated(data, locale);
    }

    @Transactional
    public String logMeal(MealEntryRequest request) {
        User user = userRepository.findByTelegramId(request.telegramId())
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);
        LocalDate today = LocalDate.now();

        // Validate meal macros
        validateMealMacros(request);

        // Calculate calories from macronutrients
        float calculatedCalories = calculator.calculateCaloriesFromMacros(request.protein(), request.carbs(), request.fat());

        MealEntry meal = MealEntry.builder()
                .user(user)
                .foodName(request.foodName())
                .calories(calculatedCalories)
                .protein(request.protein())
                .carbs(request.carbs())
                .fat(request.fat())
                .mealTime(request.mealTime())
                .date(today)
                .build();

        mealEntryRepository.save(meal);

        // Get daily totals
        Float totalCalories = mealEntryRepository.sumCaloriesByUserAndDate(user, today);
        MacroProjection macros = mealEntryRepository.sumMacrosByUserAndDate(user, today);

        // Check if profile exists for comparison
        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(request.telegramId()).orElse(null);

        int percentage = profile != null
            ? calculator.calculateCaloriePercentage(totalCalories, profile.getDailyCalorieTarget())
            : 0;

        MealLoggedData data = MealLoggedData.builder()
            .foodName(request.foodName())
            .calculatedCalories(calculatedCalories)
            .protein(request.protein())
            .fat(request.fat())
            .carbs(request.carbs())
            .totalCalories(totalCalories)
            .macros(macros)
            .profile(profile)
            .percentage(percentage)
            .build();

        return formatter.formatMealLogged(data, locale);
    }

    public String getDailySummary(Long telegramId, LocalDate date) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);
        LocalDate targetDate = date != null ? date : LocalDate.now();

        Float totalCalories = mealEntryRepository.sumCaloriesByUserAndDate(user, targetDate);
        MacroProjection macros = mealEntryRepository.sumMacrosByUserAndDate(user, targetDate);

        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(telegramId).orElse(null);

        int percentage = profile != null && totalCalories != null
            ? calculator.calculateCaloriePercentage(totalCalories, profile.getDailyCalorieTarget())
            : 0;

        return formatter.formatDailySummary(targetDate, totalCalories, macros, profile, percentage, locale);
    }

    @Transactional
    public String logWeight(WeightEntryRequest request) {
        User user = userRepository.findByTelegramId(request.telegramId())
                .orElseThrow(UserNotFoundException::new);

        // Validate weight value
        if (request.weight() < MIN_WEIGHT_KG || request.weight() > MAX_WEIGHT_KG) {
            throw new InvalidNutritionDataException("Weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg");
        }

        Locale locale = userService.getUserLocale(user);
        LocalDate date = request.date() != null ? request.date() : LocalDate.now();

        // Validate date is not in future (allow 1 day tolerance for timezone differences)
        LocalDate maxAllowedDate = LocalDate.now().plusDays(1);
        if (date.isAfter(maxAllowedDate)) {
            throw new InvalidNutritionDataException("Cannot log weight for future dates");
        }

        // Check for existing entry on same date and update if exists, otherwise create new
        WeightHistory weightEntry = weightHistoryRepository.findByUserTelegramIdAndDate(request.telegramId(), date)
                .orElse(WeightHistory.builder()
                        .user(user)
                        .date(date)
                        .build());

        weightEntry.setWeight(request.weight());
        weightHistoryRepository.save(weightEntry);

        // Update current weight in profile if logging today's weight
        if (date.equals(LocalDate.now())) {
            nutritionProfileRepository.findByUserTelegramId(request.telegramId())
                    .ifPresent(profile -> {
                        profile.setCurrentWeight(request.weight());
                        nutritionProfileRepository.save(profile);
                    });
        }

        // Get first weight to calculate change
        WeightHistory firstEntry = weightHistoryRepository.findFirstByUserTelegramIdOrderByDateAsc(request.telegramId())
                .orElse(null);

        String changeMessage;
        if (firstEntry != null && !firstEntry.getDate().equals(weightEntry.getDate())) {
            float delta = request.weight() - firstEntry.getWeight();
            changeMessage = formatter.formatWeightChange(delta, locale);
        } else {
            changeMessage = formatter.formatWeightChange(0f, locale);
        }

        return formatter.formatWeightLogged(request.weight(), changeMessage, locale);
    }

    public String getRecommendations(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);

        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(telegramId)
                .orElseThrow(() -> new NutritionProfileNotFoundException("nutrition.error.profile_not_found"));

        // Count meal logging days in last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Long mealDays = mealEntryRepository.countDistinctMealDaysByUser(user, thirtyDaysAgo);

        // Count workout frequency using optimized repository query
        Long workoutDays = exerciseRecordRepository.countDistinctWorkoutDaysByUserAfterDate(user, thirtyDaysAgo);
        float workoutsPerWeek = workoutDays / WEEKS_IN_MONTH;

        return formatter.formatRecommendations(
            profile.getGoalType(),
            mealDays,
            workoutsPerWeek,
            profile.getProteinTarget(),
            locale
        );
    }

    // Validation methods

    private void validateNutritionProfile(NutritionProfileRequest request) {
        // Weight validations
        if (request.currentWeight() < MIN_WEIGHT_KG || request.currentWeight() > MAX_WEIGHT_KG) {
            throw new InvalidNutritionDataException("Current weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg");
        }
        if (request.targetWeight() < MIN_WEIGHT_KG || request.targetWeight() > MAX_WEIGHT_KG) {
            throw new InvalidNutritionDataException("Target weight must be between " + MIN_WEIGHT_KG + " and " + MAX_WEIGHT_KG + " kg");
        }

        // Height validations
        if (request.height() < MIN_HEIGHT_CM || request.height() > MAX_HEIGHT_CM) {
            throw new InvalidNutritionDataException("Height must be between " + MIN_HEIGHT_CM + " and " + MAX_HEIGHT_CM + " cm");
        }

        // Validate weight change is reasonable
        float weightDelta = Math.abs(request.targetWeight() - request.currentWeight());
        if (weightDelta > MAX_WEIGHT_CHANGE_KG) {
            throw new InvalidNutritionDataException("Weight change goal cannot exceed " + MAX_WEIGHT_CHANGE_KG + " kg");
        }
    }

    private void validateMealMacros(MealEntryRequest request) {
        // Max macro values per meal
        if (request.protein() > MAX_PROTEIN_PER_MEAL_G) {
            throw new InvalidNutritionDataException("Protein per meal cannot exceed " + MAX_PROTEIN_PER_MEAL_G + "g");
        }
        if (request.carbs() > MAX_CARBS_PER_MEAL_G) {
            throw new InvalidNutritionDataException("Carbs per meal cannot exceed " + MAX_CARBS_PER_MEAL_G + "g");
        }
        if (request.fat() > MAX_FAT_PER_MEAL_G) {
            throw new InvalidNutritionDataException("Fat per meal cannot exceed " + MAX_FAT_PER_MEAL_G + "g");
        }

        // Calculate total calories and validate reasonable meal size
        float totalCalories = calculator.calculateCaloriesFromMacros(request.protein(), request.carbs(), request.fat());
        if (totalCalories > MAX_CALORIES_PER_MEAL) {
            throw new InvalidNutritionDataException("Meal cannot exceed " + MAX_CALORIES_PER_MEAL + " kcal");
        }

        // Validate at least one macro is present
        if (request.protein() < MIN_MACRO_VALUE && request.carbs() < MIN_MACRO_VALUE && request.fat() < MIN_MACRO_VALUE) {
            throw new InvalidNutritionDataException("At least one macronutrient must be greater than zero");
        }
    }
}
