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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class NutritionService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final float CALORIES_PER_KG = 7700f; // kcal per kg of body fat

    private final UserRepository userRepository;
    private final NutritionProfileRepository nutritionProfileRepository;
    private final MealEntryRepository mealEntryRepository;
    private final WeightHistoryRepository weightHistoryRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MessageLocalizer messageLocalizer;
    private final UserService userService;

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
        GoalType goalType = determineGoalType(request.currentWeight(), request.targetWeight());
        float bmr = calculateBMR(user.getSex(), user.getAge(), request.currentWeight(), request.height());
        float tdee = calculateTDEE(bmr, request.activityLevel());
        float dailyCalories = adjustCaloriesForGoal(tdee, goalType, request.weightChangeSpeed());
        MacroTargets macros = calculateMacroTargets(dailyCalories, goalType);
        LocalDate goalDeadline = calculateGoalDeadline(
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
        profile.setProteinTarget(macros.protein);
        profile.setCarbsTarget(macros.carbs);
        profile.setFatTarget(macros.fat);
        profile.setGoalDeadline(goalDeadline);

        nutritionProfileRepository.save(profile);

        // Format response
        float weightDelta = request.targetWeight() - request.currentWeight();
        String goalTypeLocalized = messageLocalizer.localize("nutrition.goal." + goalType.name().toLowerCase(), new Object[]{}, locale);
        String activityLevelLocalized = messageLocalizer.localize("nutrition.activity." + request.activityLevel().name().toLowerCase(), new Object[]{}, locale);

        return messageLocalizer.localize("nutrition.profile.created", new Object[]{
            request.currentWeight(),
            request.targetWeight(),
            Math.abs(weightDelta),
            goalDeadline.format(DATE_FORMATTER),
            request.weightChangeSpeed().getKgPerWeek(),
            Math.round(dailyCalories),
            Math.round(macros.protein),
            Math.round(macros.fat),
            Math.round(macros.carbs),
            activityLevelLocalized,
            goalTypeLocalized
        }, locale);
    }

    @Transactional
    public String logMeal(MealEntryRequest request) {
        User user = userRepository.findByTelegramId(request.telegramId())
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);
        LocalDate today = LocalDate.now();

        // Validate meal macros
        validateMealMacros(request);

        // Calculate calories from macronutrients: protein(4 kcal/g) + carbs(4 kcal/g) + fat(9 kcal/g)
        float calculatedCalories = (request.protein() * 4) + (request.carbs() * 4) + (request.fat() * 9);

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

        if (profile != null) {
            int percentage = Math.round((totalCalories / profile.getDailyCalorieTarget()) * 100);

            return messageLocalizer.localize("nutrition.meal.logged", new Object[]{
                request.foodName(),
                Math.round(calculatedCalories),
                Math.round(request.protein()),
                Math.round(request.fat()),
                Math.round(request.carbs()),
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
            return messageLocalizer.localize("nutrition.meal.logged.no_profile", new Object[]{
                request.foodName(),
                Math.round(calculatedCalories),
                Math.round(request.protein()),
                Math.round(request.fat()),
                Math.round(request.carbs())
            }, locale);
        }
    }

    public String getDailySummary(Long telegramId, LocalDate date) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);
        LocalDate targetDate = date != null ? date : LocalDate.now();

        Float totalCalories = mealEntryRepository.sumCaloriesByUserAndDate(user, targetDate);
        MacroProjection macros = mealEntryRepository.sumMacrosByUserAndDate(user, targetDate);

        if (totalCalories == 0) {
            return messageLocalizer.localize("nutrition.summary.no_meals", new Object[]{}, locale);
        }

        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(telegramId).orElse(null);

        if (profile != null) {
            int percentage = Math.round((totalCalories / profile.getDailyCalorieTarget()) * 100);

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

    @Transactional
    public String logWeight(WeightEntryRequest request) {
        User user = userRepository.findByTelegramId(request.telegramId())
                .orElseThrow(UserNotFoundException::new);

        // Validate weight value
        if (request.weight() < 25 || request.weight() > 300) {
            throw new InvalidNutritionDataException("Weight must be between 25 and 300 kg");
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
        WeightHistory firstEntry = weightHistoryRepository.findByUserTelegramIdOrderByDateDesc(request.telegramId())
                .stream()
                .reduce((first, second) -> second)
                .orElse(null);

        String changeMessage;
        if (firstEntry != null && !firstEntry.getId().equals(weightEntry.getId())) {
            float delta = request.weight() - firstEntry.getWeight();
            if (Math.abs(delta) < 0.1f) {
                changeMessage = messageLocalizer.localize("nutrition.weight.change.same", new Object[]{}, locale);
            } else if (delta < 0) {
                changeMessage = messageLocalizer.localize("nutrition.weight.change.loss", new Object[]{Math.abs(delta)}, locale);
            } else {
                changeMessage = messageLocalizer.localize("nutrition.weight.change.gain", new Object[]{delta}, locale);
            }
        } else {
            changeMessage = messageLocalizer.localize("nutrition.weight.change.same", new Object[]{}, locale);
        }

        return messageLocalizer.localize("nutrition.weight.logged", new Object[]{
            request.weight(),
            changeMessage
        }, locale);
    }

    public String getRecommendations(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        Locale locale = userService.getUserLocale(user);

        NutritionProfile profile = nutritionProfileRepository.findByUserTelegramId(telegramId)
                .orElseThrow(() -> new NutritionProfileNotFoundException("nutrition.error.profile_not_found"));

        String goalTypeLocalized = messageLocalizer.localize("nutrition.goal." + profile.getGoalType().name().toLowerCase(), new Object[]{}, locale);

        // Count meal logging days in last 30 days
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        Long mealDays = mealEntryRepository.countDistinctMealDaysByUser(user, thirtyDaysAgo);

        // Count workout frequency using optimized repository query
        Long workoutDays = exerciseRecordRepository.countDistinctWorkoutDaysByUserAfterDate(user, thirtyDaysAgo);
        float workoutsPerWeek = workoutDays / 4.3f;

        StringBuilder recommendations = new StringBuilder();
        recommendations.append(messageLocalizer.localize("nutrition.rec.header", new Object[]{}, locale)).append("\n");
        recommendations.append(messageLocalizer.localize("nutrition.rec.goal", new Object[]{goalTypeLocalized}, locale)).append("\n");
        recommendations.append(messageLocalizer.localize("nutrition.rec.progress", new Object[]{mealDays}, locale)).append("\n");

        // Consistency advice
        if (mealDays >= 20) {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.consistent", new Object[]{}, locale)).append("\n");
        } else {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.start", new Object[]{}, locale)).append("\n");
        }

        // Protein advice based on workouts
        if (workoutsPerWeek >= 3) {
            recommendations.append(messageLocalizer.localize("nutrition.rec.advice.protein", new Object[]{
                Math.round(workoutsPerWeek),
                Math.round(profile.getProteinTarget())
            }, locale));
        }

        return recommendations.toString();
    }

    // Calculator methods

    private GoalType determineGoalType(Float currentWeight, Float targetWeight) {
        float diff = targetWeight - currentWeight;
        if (Math.abs(diff) <= 2.0f) {
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
    private float calculateBMR(Sex sex, Integer age, Float weight, Float height) {
        float bmr = (10 * weight) + (6.25f * height) - (5 * age);
        return sex == Sex.MAN ? bmr + 5 : bmr - 161;
    }

    private float calculateTDEE(float bmr, ActivityLevel activityLevel) {
        return switch (activityLevel) {
            case SEDENTARY -> bmr * 1.2f;
            case LIGHT -> bmr * 1.375f;
            case MODERATE -> bmr * 1.55f;
            case ACTIVE -> bmr * 1.725f;
            case VERY_ACTIVE -> bmr * 1.9f;
        };
    }

    private float adjustCaloriesForGoal(float tdee, GoalType goalType, WeightChangeSpeed speed) {
        if (goalType == GoalType.MAINTAIN) {
            return tdee;
        }

        float weeklyCalorieDelta = speed.getKgPerWeek() * CALORIES_PER_KG;
        float dailyCalorieDelta = weeklyCalorieDelta / 7;

        return goalType == GoalType.LOSS ? tdee - dailyCalorieDelta : tdee + dailyCalorieDelta;
    }

    private MacroTargets calculateMacroTargets(float dailyCalories, GoalType goalType) {
        float proteinPercent, carbsPercent, fatPercent;

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

        float proteinGrams = (dailyCalories * proteinPercent) / 4;
        float carbsGrams = (dailyCalories * carbsPercent) / 4;
        float fatGrams = (dailyCalories * fatPercent) / 9;

        return new MacroTargets(proteinGrams, carbsGrams, fatGrams);
    }

    private LocalDate calculateGoalDeadline(Float currentWeight, Float targetWeight, WeightChangeSpeed speed) {
        float weightDelta = Math.abs(targetWeight - currentWeight);

        // If weight delta is negligible (maintenance goal), return today
        if (weightDelta < 0.1f) {
            return LocalDate.now();
        }

        float weeks = weightDelta / speed.getKgPerWeek();
        long days = Math.round(weeks * 7);

        // Cap maximum goal deadline at 2 years (730 days)
        if (days > 730) {
            days = 730;
        }

        return LocalDate.now().plusDays(days);
    }

    // Validation methods

    private void validateNutritionProfile(NutritionProfileRequest request) {
        // Weight validations (25-300 kg range)
        if (request.currentWeight() < 25 || request.currentWeight() > 300) {
            throw new InvalidNutritionDataException("Current weight must be between 25 and 300 kg");
        }
        if (request.targetWeight() < 25 || request.targetWeight() > 300) {
            throw new InvalidNutritionDataException("Target weight must be between 25 and 300 kg");
        }

        // Height validations (100-250 cm range)
        if (request.height() < 100 || request.height() > 250) {
            throw new InvalidNutritionDataException("Height must be between 100 and 250 cm");
        }

        // Validate weight change is reasonable (max 100kg difference)
        float weightDelta = Math.abs(request.targetWeight() - request.currentWeight());
        if (weightDelta > 100) {
            throw new InvalidNutritionDataException("Weight change goal cannot exceed 100 kg");
        }
    }

    private void validateMealMacros(MealEntryRequest request) {
        // Max macro values per meal (reasonable upper bounds)
        if (request.protein() > 200) {
            throw new InvalidNutritionDataException("Protein per meal cannot exceed 200g");
        }
        if (request.carbs() > 500) {
            throw new InvalidNutritionDataException("Carbs per meal cannot exceed 500g");
        }
        if (request.fat() > 200) {
            throw new InvalidNutritionDataException("Fat per meal cannot exceed 200g");
        }

        // Calculate total calories and validate reasonable meal size (max 5000 kcal)
        float totalCalories = (request.protein() * 4) + (request.carbs() * 4) + (request.fat() * 9);
        if (totalCalories > 5000) {
            throw new InvalidNutritionDataException("Meal cannot exceed 5000 kcal");
        }

        // Validate at least one macro is present
        if (request.protein() == 0 && request.carbs() == 0 && request.fat() == 0) {
            throw new InvalidNutritionDataException("At least one macronutrient must be greater than zero");
        }
    }

    private record MacroTargets(float protein, float carbs, float fat) {}
}
