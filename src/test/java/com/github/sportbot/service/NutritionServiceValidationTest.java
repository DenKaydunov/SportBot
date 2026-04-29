package com.github.sportbot.service;

import com.github.sportbot.dto.MealEntryRequest;
import com.github.sportbot.dto.NutritionProfileRequest;
import com.github.sportbot.dto.WeightEntryRequest;
import com.github.sportbot.exception.InvalidNutritionDataException;
import com.github.sportbot.model.ActivityLevel;
import com.github.sportbot.model.Sex;
import com.github.sportbot.model.User;
import com.github.sportbot.model.WeightChangeSpeed;
import com.github.sportbot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NutritionServiceValidationTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NutritionProfileRepository nutritionProfileRepository;
    @Mock
    private MealEntryRepository mealEntryRepository;
    @Mock
    private WeightHistoryRepository weightHistoryRepository;
    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;
    @Mock
    private UserService userService;
    @Mock
    private NutritionCalculator calculator;
    @Mock
    private NutritionResponseFormatter formatter;

    @InjectMocks
    private NutritionService nutritionService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .telegramId(1000001L)
                .age(30)
                .sex(Sex.MAN)
                .language("ru")
                .build();

        // Setup lenient calculator mocks for validation methods
        lenient().when(calculator.calculateCaloriesFromMacros(anyFloat(), anyFloat(), anyFloat()))
                .thenAnswer(invocation -> {
                    float protein = invocation.getArgument(0);
                    float carbs = invocation.getArgument(1);
                    float fat = invocation.getArgument(2);
                    return (protein * 4f) + (carbs * 4f) + (fat * 9f);
                });

        // Setup lenient formatter mocks
        lenient().when(formatter.formatWeightChange(anyFloat(), any(Locale.class)))
                .thenReturn("Вес не изменился");
        lenient().when(userService.getUserLocale(testUser))
                .thenReturn(new Locale("ru"));
    }

    // Weight validations

    @Test
    void testCreateProfile_CurrentWeightTooLow() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                20.0f,  // below 25kg minimum
                180.0f,
                75.0f,
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    @Test
    void testCreateProfile_CurrentWeightTooHigh() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                350.0f,  // above 300kg maximum
                180.0f,
                75.0f,
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    @Test
    void testCreateProfile_TargetWeightTooLow() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                80.0f,
                180.0f,
                20.0f,  // below 25kg minimum
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    @Test
    void testCreateProfile_TargetWeightTooHigh() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                80.0f,
                180.0f,
                350.0f,  // above 300kg maximum
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    // Height validations

    @Test
    void testCreateProfile_HeightTooLow() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                75.0f,
                80.0f,  // below 100cm minimum
                70.0f,
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    @Test
    void testCreateProfile_HeightTooHigh() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                75.0f,
                300.0f,  // above 250cm maximum
                70.0f,
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    // Weight change validations

    @Test
    void testCreateProfile_WeightChangeExceedsLimit() {
        NutritionProfileRequest request = new NutritionProfileRequest(
                1000001L,
                150.0f,
                180.0f,
                40.0f,  // 110kg difference (exceeds 100kg limit)
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    // Meal macro validations

    @Test
    void testLogMeal_ProteinExceedsLimit() {
        MealEntryRequest request = new MealEntryRequest(
                1000001L,
                "Protein shake",
                250.0f,  // exceeds 200g limit
                0.0f,
                0.0f,
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logMeal(request)
        );
    }

    @Test
    void testLogMeal_CarbsExceedsLimit() {
        MealEntryRequest request = new MealEntryRequest(
                1000001L,
                "Pasta feast",
                0.0f,
                600.0f,  // exceeds 500g limit
                0.0f,
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logMeal(request)
        );
    }

    @Test
    void testLogMeal_FatExceedsLimit() {
        MealEntryRequest request = new MealEntryRequest(
                1000001L,
                "Oil",
                0.0f,
                0.0f,
                250.0f,  // exceeds 200g limit
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logMeal(request)
        );
    }

    @Test
    void testLogMeal_EdgeCaseMaxMacrosUnderCalorieLimit() {
        // Test edge case: all macros at max but under total calorie limit (should pass)
        MealEntryRequest edgeCaseRequest = new MealEntryRequest(
                1000001L,
                "Edge case",
                200.0f,  // 800 kcal (max protein)
                500.0f,  // 2000 kcal (max carbs)
                200.0f,  // 1800 kcal (max fat)
                null     // total: 4600 kcal (under 5000 limit, should pass validation)
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));
        when(mealEntryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        when(mealEntryRepository.sumCaloriesByUserAndDate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any())).thenReturn(4600f);
        when(mealEntryRepository.sumMacrosByUserAndDate(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new com.github.sportbot.repository.MacroProjection() {
                    @Override
                    public Float getProtein() { return 200f; }
                    @Override
                    public Float getCarbs() { return 500f; }
                    @Override
                    public Float getFat() { return 200f; }
                });
        when(nutritionProfileRepository.findByUserTelegramId(1000001L)).thenReturn(Optional.empty());
        when(formatter.formatMealLogged(org.mockito.ArgumentMatchers.any(MealLoggedData.class),
                org.mockito.ArgumentMatchers.any(Locale.class)))
                .thenReturn("Meal logged: test");

        // When: log meal at edge of limits
        String result = nutritionService.logMeal(edgeCaseRequest);

        // Then: should NOT throw and return valid result
        assertNotNull(result);
        assertTrue(result.contains("test"));
    }

    @Test
    void testLogMeal_AllMacrosZero() {
        MealEntryRequest request = new MealEntryRequest(
                1000001L,
                "Empty food",
                0.0f,
                0.0f,
                0.0f,
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logMeal(request)
        );
    }

    // Weight logging validations

    @Test
    void testLogWeight_TooLow() {
        WeightEntryRequest request = new WeightEntryRequest(
                1000001L,
                20.0f,  // below 25kg
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logWeight(request)
        );
    }

    @Test
    void testLogWeight_TooHigh() {
        WeightEntryRequest request = new WeightEntryRequest(
                1000001L,
                350.0f,  // above 300kg
                null
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logWeight(request)
        );
    }

    @Test
    void testLogWeight_FutureDate() {
        WeightEntryRequest request = new WeightEntryRequest(
                1000001L,
                75.0f,
                LocalDate.now().plusDays(5)  // 5 days in future (beyond timezone tolerance)
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));

        assertThrows(InvalidNutritionDataException.class, () ->
                nutritionService.logWeight(request)
        );
    }

    @Test
    void testLogWeight_TomorrowDateAllowed() {
        // Given: tomorrow's date (within timezone tolerance)
        WeightEntryRequest request = new WeightEntryRequest(
                1000001L,
                75.0f,
                LocalDate.now().plusDays(1)
        );

        when(userRepository.findByTelegramId(1000001L)).thenReturn(Optional.of(testUser));
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));
        when(weightHistoryRepository.findByUserTelegramIdAndDate(1000001L, LocalDate.now().plusDays(1)))
                .thenReturn(Optional.empty());
        when(weightHistoryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(null);
        when(weightHistoryRepository.findFirstByUserTelegramIdOrderByDateAsc(1000001L))
                .thenReturn(Optional.empty());
        when(formatter.formatWeightLogged(org.mockito.ArgumentMatchers.anyFloat(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn("test");
        when(formatter.formatWeightChange(org.mockito.ArgumentMatchers.anyFloat(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn("test");

        // When/Then: should NOT throw exception (timezone tolerance)
        String result = nutritionService.logWeight(request);
        assertNotNull(result);
    }
}
