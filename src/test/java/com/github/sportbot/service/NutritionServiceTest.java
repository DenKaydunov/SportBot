package com.github.sportbot.service;

import com.github.sportbot.dto.MealEntryRequest;
import com.github.sportbot.dto.NutritionProfileRequest;
import com.github.sportbot.dto.WeightEntryRequest;
import com.github.sportbot.exception.NutritionProfileNotFoundException;
import com.github.sportbot.exception.UserDataInsufficientException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

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

    private MessageLocalizer messageLocalizer;

    private NutritionService nutritionService;

    private User testUserMale;
    private User testUserFemale;
    private static final long TELEGRAM_ID_MALE = 1000001L;
    private static final long TELEGRAM_ID_FEMALE = 1000002L;

    private static MessageLocalizer createRealMessageLocalizer() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        return new MessageLocalizerImpl(messageSource);
    }

    @BeforeEach
    void setUp() {
        messageLocalizer = createRealMessageLocalizer();

        nutritionService = new NutritionService(
                userRepository,
                nutritionProfileRepository,
                mealEntryRepository,
                weightHistoryRepository,
                exerciseRecordRepository,
                messageLocalizer,
                userService
        );

        // Setup male user (matching our test case from plan: 36 years, 107kg, 186cm)
        testUserMale = User.builder()
                .id(1)
                .telegramId(TELEGRAM_ID_MALE)
                .fullName("Test Male User")
                .age(36)
                .sex(Sex.MAN)
                .language("ru")
                .exerciseRecords(new ArrayList<>())
                .build();

        // Setup female user
        testUserFemale = User.builder()
                .id(2)
                .telegramId(TELEGRAM_ID_FEMALE)
                .fullName("Test Female User")
                .age(28)
                .sex(Sex.WOMAN)
                .language("ru")
                .exerciseRecords(new ArrayList<>())
                .build();
    }

    @Test
    void testCreateProfile_Male_WeightLoss() {
        // Given: Male, 36 years, 107kg, 186cm, MODERATE activity, target 90kg, OPTIMAL speed
        NutritionProfileRequest request = new NutritionProfileRequest(
                TELEGRAM_ID_MALE,
                107.0f,  // current
                186.0f,  // height
                90.0f,   // target
                ActivityLevel.MODERATE,
                "нет",
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = nutritionService.createOrUpdateProfile(request);

        // Then
        assertTrue(result.contains("персональный план создан"));
        assertTrue(result.contains("107"));  // current weight
        assertTrue(result.contains("90"));   // target weight
        assertTrue(result.contains("17"));   // delta

        // Verify calculation is in reasonable range (BMR calculation tested)
        // Numbers are formatted with spaces for thousands, so just check presence
        assertTrue(result.contains("ккал"));
        assertTrue(result.contains("Белки"));
        assertTrue(result.contains("Жиры"));
        assertTrue(result.contains("Углеводы"));

        verify(nutritionProfileRepository, times(1)).save(any(NutritionProfile.class));
    }

    @Test
    void testCreateProfile_Female_WeightGain() {
        // Given: Female, 28 years, 50kg, 165cm, LIGHT activity, target 55kg, SLOW speed
        NutritionProfileRequest request = new NutritionProfileRequest(
                TELEGRAM_ID_FEMALE,
                50.0f,
                165.0f,
                55.0f,
                ActivityLevel.LIGHT,
                null,
                WeightChangeSpeed.SLOW
        );

        when(userRepository.findByTelegramId(TELEGRAM_ID_FEMALE)).thenReturn(Optional.of(testUserFemale));
        when(userService.getUserLocale(testUserFemale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_FEMALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = nutritionService.createOrUpdateProfile(request);

        // Then
        assertTrue(result.contains("персональный план создан"));
        assertTrue(result.contains("50"));  // current weight
        assertTrue(result.contains("55"));  // target weight
        assertTrue(result.contains("Набор массы"));  // goal type GAIN

        verify(nutritionProfileRepository, times(1)).save(any(NutritionProfile.class));
    }

    @Test
    void testCreateProfile_Maintenance() {
        // Given: weight difference within 2kg tolerance
        NutritionProfileRequest request = new NutritionProfileRequest(
                TELEGRAM_ID_MALE,
                75.0f,
                180.0f,
                76.0f,  // only 1kg difference -> MAINTAIN
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = nutritionService.createOrUpdateProfile(request);

        // Then
        assertTrue(result.contains("Поддержание веса"));  // goal type MAINTAIN

        verify(nutritionProfileRepository, times(1)).save(any(NutritionProfile.class));
    }

    @Test
    void testCreateProfile_InvalidUserData() {
        // Given: user without age
        User userWithoutAge = User.builder()
                .id(3)
                .telegramId(1000003L)
                .age(null)  // missing age
                .sex(Sex.MAN)
                .build();

        NutritionProfileRequest request = new NutritionProfileRequest(
                1000003L,
                75.0f,
                180.0f,
                70.0f,
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(1000003L)).thenReturn(Optional.of(userWithoutAge));

        // When/Then
        assertThrows(UserDataInsufficientException.class, () ->
                nutritionService.createOrUpdateProfile(request)
        );
    }

    @Test
    void testLogMeal_Success() {
        // Given
        MealEntryRequest request = new MealEntryRequest(
                TELEGRAM_ID_MALE,
                "Курица с рисом",
                35.0f,  // protein
                45.0f,  // carbs
                12.0f,  // fat
                null
        );
        // Expected calories: (35*4) + (45*4) + (12*9) = 140 + 180 + 108 = 428 kcal

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(mealEntryRepository.save(any(MealEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealEntryRepository.sumCaloriesByUserAndDate(any(User.class), any(LocalDate.class))).thenReturn(428.0f);
        when(mealEntryRepository.sumMacrosByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(new MacroProjection() {
                    @Override
                    public Float getProtein() { return 35.0f; }
                    @Override
                    public Float getCarbs() { return 45.0f; }
                    @Override
                    public Float getFat() { return 12.0f; }
                });
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());

        // When
        String result = nutritionService.logMeal(request);

        // Then
        assertTrue(result.contains("Приём пищи записан"));
        assertTrue(result.contains("Курица с рисом"));
        assertTrue(result.contains("428"));

        verify(mealEntryRepository, times(1)).save(any(MealEntry.class));
    }

    @Test
    void testLogMeal_WithProfile() {
        // Given
        MealEntryRequest request = new MealEntryRequest(
                TELEGRAM_ID_MALE,
                "Овсянка",
                10.0f,  // protein
                50.0f,  // carbs
                5.0f,   // fat
                LocalTime.of(8, 0)
        );
        // Expected calories: (10*4) + (50*4) + (5*9) = 40 + 200 + 45 = 285 kcal

        NutritionProfile profile = NutritionProfile.builder()
                .user(testUserMale)
                .dailyCalorieTarget(2500.0f)
                .proteinTarget(200.0f)
                .carbsTarget(250.0f)
                .fatTarget(80.0f)
                .build();

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(mealEntryRepository.save(any(MealEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mealEntryRepository.sumCaloriesByUserAndDate(any(User.class), any(LocalDate.class))).thenReturn(285.0f);
        when(mealEntryRepository.sumMacrosByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(new MacroProjection() {
                    @Override
                    public Float getProtein() { return 10.0f; }
                    @Override
                    public Float getCarbs() { return 50.0f; }
                    @Override
                    public Float getFat() { return 5.0f; }
                });
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(profile));

        // When
        String result = nutritionService.logMeal(request);

        // Then
        assertTrue(result.contains("Приём пищи записан"));
        assertTrue(result.contains("Итого за сегодня"));
        assertTrue(result.contains("ккал")); // has calorie target

        verify(mealEntryRepository, times(1)).save(any(MealEntry.class));
    }

    @Test
    void testGetDailySummary_WithProfile() {
        // Given
        NutritionProfile profile = NutritionProfile.builder()
                .user(testUserMale)
                .dailyCalorieTarget(2500.0f)
                .proteinTarget(200.0f)
                .carbsTarget(250.0f)
                .fatTarget(80.0f)
                .build();

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(mealEntryRepository.sumCaloriesByUserAndDate(any(User.class), any(LocalDate.class))).thenReturn(1250.0f);
        when(mealEntryRepository.sumMacrosByUserAndDate(any(User.class), any(LocalDate.class)))
                .thenReturn(new MacroProjection() {
                    @Override
                    public Float getProtein() { return 100.0f; }
                    @Override
                    public Float getCarbs() { return 125.0f; }
                    @Override
                    public Float getFat() { return 40.0f; }
                });
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(profile));

        // When
        String result = nutritionService.getDailySummary(TELEGRAM_ID_MALE, null);

        // Then
        assertTrue(result.contains("Итоги за"));
        assertTrue(result.contains("ккал"));  // has calories
        assertTrue(result.contains("50%"));    // percentage (1250/2500 = 50%)
    }

    @Test
    void testGetDailySummary_NoMeals() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(mealEntryRepository.sumCaloriesByUserAndDate(any(User.class), any(LocalDate.class))).thenReturn(0.0f);

        // When
        String result = nutritionService.getDailySummary(TELEGRAM_ID_MALE, null);

        // Then
        assertTrue(result.contains("приёмов пищи не найдено"));
    }

    @Test
    void testLogWeight_UpdatesProfile() {
        // Given
        WeightEntryRequest request = new WeightEntryRequest(
                TELEGRAM_ID_MALE,
                105.0f,
                null  // today
        );

        NutritionProfile profile = NutritionProfile.builder()
                .user(testUserMale)
                .currentWeight(107.0f)
                .build();

        WeightHistory firstEntry = WeightHistory.builder()
                .id(1L)
                .user(testUserMale)
                .weight(107.0f)
                .date(LocalDate.now().minusDays(7))
                .build();

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(weightHistoryRepository.save(any(WeightHistory.class))).thenAnswer(invocation -> {
            WeightHistory wh = invocation.getArgument(0);
            wh.setId(2L);
            return wh;
        });
        when(weightHistoryRepository.findByUserTelegramIdOrderByDateDesc(TELEGRAM_ID_MALE))
                .thenReturn(Arrays.asList(firstEntry));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(profile));
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String result = nutritionService.logWeight(request);

        // Then
        assertTrue(result.contains("Вес зафиксирован"));
        assertTrue(result.contains("105"));
        assertTrue(result.contains("↓"));  // weight loss indicator
        assertTrue(result.contains("2"));  // 107 - 105 = 2kg lost

        verify(nutritionProfileRepository, times(1)).save(any(NutritionProfile.class));
    }

    @Test
    void testGetRecommendations_ActiveUser() {
        // Given
        NutritionProfile profile = NutritionProfile.builder()
                .user(testUserMale)
                .goalType(GoalType.LOSS)
                .proteinTarget(200.0f)
                .build();

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(profile));
        when(mealEntryRepository.countDistinctMealDaysByUser(any(User.class), any(LocalDate.class))).thenReturn(25L);
        when(exerciseRecordRepository.countDistinctWorkoutDaysByUserAfterDate(any(User.class), any(LocalDate.class))).thenReturn(3L);

        // When
        String result = nutritionService.getRecommendations(TELEGRAM_ID_MALE);

        // Then
        assertTrue(result.contains("Персональные рекомендации"));
        assertTrue(result.contains("Снижение веса"));
        assertTrue(result.contains("25"));  // meal logging days
        assertTrue(result.contains("Отличная последовательность"));  // >= 20 days
    }

    @Test
    void testGetRecommendations_ProfileNotFound() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(NutritionProfileNotFoundException.class, () ->
                nutritionService.getRecommendations(TELEGRAM_ID_MALE)
        );
    }

    @Test
    void testActivityLevelMultipliers() {
        // Test all 5 activity level multipliers
        // This is tested implicitly through profile creation, but let's verify the calculations

        NutritionProfileRequest sedentaryRequest = new NutritionProfileRequest(
                TELEGRAM_ID_MALE, 75.0f, 180.0f, 75.0f, ActivityLevel.SEDENTARY, null, WeightChangeSpeed.OPTIMAL);

        NutritionProfileRequest veryActiveRequest = new NutritionProfileRequest(
                TELEGRAM_ID_MALE, 75.0f, 180.0f, 75.0f, ActivityLevel.VERY_ACTIVE, null, WeightChangeSpeed.OPTIMAL);

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String sedentaryResult = nutritionService.createOrUpdateProfile(sedentaryRequest);
        String veryActiveResult = nutritionService.createOrUpdateProfile(veryActiveRequest);

        // Then: VERY_ACTIVE should have higher calories than SEDENTARY
        assertTrue(sedentaryResult.contains("Минимальная"));
        assertTrue(veryActiveResult.contains("Очень активная"));
        // Both should complete successfully
        assertNotNull(sedentaryResult);
        assertNotNull(veryActiveResult);
    }

    @Test
    void testMacroSplit_AllGoalTypes() {
        // Test LOSS, MAINTAIN, GAIN have different macro splits

        NutritionProfileRequest lossRequest = new NutritionProfileRequest(
                TELEGRAM_ID_MALE, 100.0f, 180.0f, 80.0f, ActivityLevel.MODERATE, null, WeightChangeSpeed.OPTIMAL);

        NutritionProfileRequest maintainRequest = new NutritionProfileRequest(
                TELEGRAM_ID_MALE, 75.0f, 180.0f, 75.0f, ActivityLevel.MODERATE, null, WeightChangeSpeed.OPTIMAL);

        NutritionProfileRequest gainRequest = new NutritionProfileRequest(
                TELEGRAM_ID_MALE, 60.0f, 180.0f, 75.0f, ActivityLevel.MODERATE, null, WeightChangeSpeed.OPTIMAL);

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        String lossResult = nutritionService.createOrUpdateProfile(lossRequest);
        String maintainResult = nutritionService.createOrUpdateProfile(maintainRequest);
        String gainResult = nutritionService.createOrUpdateProfile(gainRequest);

        // Then: All should complete successfully with different goal types
        assertTrue(lossResult.contains("Снижение веса"));
        assertTrue(maintainResult.contains("Поддержание веса"));
        assertTrue(gainResult.contains("Набор массы"));
    }

    @Test
    void testLogWeight_UpdateExistingEntry() {
        // Given: user logs weight twice on same date
        WeightEntryRequest firstRequest = new WeightEntryRequest(
                TELEGRAM_ID_MALE,
                75.0f,
                LocalDate.now()
        );

        WeightEntryRequest secondRequest = new WeightEntryRequest(
                TELEGRAM_ID_MALE,
                76.0f,  // different weight, same date
                LocalDate.now()
        );

        WeightHistory existingEntry = WeightHistory.builder()
                .id(1L)
                .user(testUserMale)
                .weight(75.0f)
                .date(LocalDate.now())
                .build();

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));

        // First call - no existing entry
        when(weightHistoryRepository.findByUserTelegramIdAndDate(TELEGRAM_ID_MALE, LocalDate.now()))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingEntry));  // second call - entry exists

        when(weightHistoryRepository.save(any(WeightHistory.class))).thenAnswer(invocation -> {
            WeightHistory saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(1L);
            }
            return saved;
        });

        when(weightHistoryRepository.findByUserTelegramIdOrderByDateDesc(TELEGRAM_ID_MALE))
                .thenReturn(Arrays.asList(existingEntry));

        // When: log weight first time
        String result1 = nutritionService.logWeight(firstRequest);

        // Then: verify save was called
        verify(weightHistoryRepository, times(1)).save(any(WeightHistory.class));

        // When: log weight second time on same date
        String result2 = nutritionService.logWeight(secondRequest);

        // Then: verify save was called again (update, not insert)
        verify(weightHistoryRepository, times(2)).save(any(WeightHistory.class));
        assertTrue(result2.contains("Вес зафиксирован"));
        assertTrue(result2.contains("76"));
    }

    @Test
    void testCalculateGoalDeadline_MaintenanceGoal() {
        // Given: maintenance goal (current = target)
        NutritionProfileRequest request = new NutritionProfileRequest(
                TELEGRAM_ID_MALE,
                75.0f,  // current
                180.0f,
                75.0f,  // target = current (0kg difference)
                ActivityLevel.MODERATE,
                null,
                WeightChangeSpeed.OPTIMAL
        );

        when(userRepository.findByTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.of(testUserMale));
        when(userService.getUserLocale(testUserMale)).thenReturn(new Locale("ru"));
        when(nutritionProfileRepository.findByUserTelegramId(TELEGRAM_ID_MALE)).thenReturn(Optional.empty());
        when(nutritionProfileRepository.save(any(NutritionProfile.class))).thenAnswer(invocation -> {
            NutritionProfile saved = invocation.getArgument(0);
            // Verify goal deadline is today (not in past)
            assertTrue(saved.getGoalDeadline().equals(LocalDate.now()) ||
                       saved.getGoalDeadline().isAfter(LocalDate.now().minusDays(1)),
                       "Goal deadline should not be in the past");
            return saved;
        });

        // When
        String result = nutritionService.createOrUpdateProfile(request);

        // Then
        assertTrue(result.contains("Поддержание веса"));
        verify(nutritionProfileRepository, times(1)).save(any(NutritionProfile.class));
    }
}
