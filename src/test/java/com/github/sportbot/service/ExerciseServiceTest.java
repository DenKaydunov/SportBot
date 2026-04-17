package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExerciseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseTypeRepository exerciseTypeRepository;

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    private MessageLocalizer messageLocalizer;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private RankService rankService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StreakService streakService;

    @Mock
    private AchievementService achievementService;

    @Mock
    private UserService userService;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @Mock
    private UnifiedAchievementService unifiedAchievementService;

    @Mock
    private AchievementDefinitionRepository achievementDefinitionRepository;

    private ExerciseService exerciseService;

    private User testUser;
    private ExerciseType testExerciseType;
    private ExerciseEntryRequest testRequest;
    private static final long TELEGRAM_ID = 123456L;

    private static MessageLocalizer createRealMessageLocalizer() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        messageSource.setDefaultLocale(Locale.forLanguageTag("en"));
        return new MessageLocalizerImpl(messageSource);
    }

    @BeforeEach
    void setUp() {
        testUser =
                User.builder()
                        .id(1)
                        .telegramId(TELEGRAM_ID)
                        .isSubscribed(true)
                        .currentStreak(9)
                        .lastWorkoutDate(LocalDate.now().minusDays(1))
                        .exerciseRecords(new ArrayList<>())
                        .maxHistory(new ArrayList<>())
                        .language("ru")
                        .build();

        testExerciseType = ExerciseType.builder().id(1L).code("pushup").title("Отжимания").build();

        testRequest = new ExerciseEntryRequest(TELEGRAM_ID, "push_up", 10);

        // Initialize real MessageLocalizer
        messageLocalizer = createRealMessageLocalizer();

        // Manually create ExerciseService with all dependencies
        exerciseService = new ExerciseService(
            userRepository,
            exerciseRecordRepository,
            messageLocalizer,
            exerciseTypeService,
            rankService,
            streakService,
            achievementService,
            notificationService,
            userService,
            entityLocalizationService,
            unifiedAchievementService,
            achievementDefinitionRepository
        );

        // Setup common message source mocks
        Locale ruLocale = Locale.forLanguageTag("ru");
        lenient().when(userService.getUserLocale(any(User.class))).thenReturn(ruLocale);

        // Setup EntityLocalizationService mocks
        lenient().when(entityLocalizationService.getExerciseTypeTitle(any(ExerciseType.class), any(Locale.class)))
                .thenAnswer(inv -> ((ExerciseType) inv.getArgument(0)).getTitle());

        // Mock new unified achievement system
        lenient().when(unifiedAchievementService.getCompletedAchievements(anyInt())).thenReturn(new ArrayList<>());
        lenient().when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(any())).thenReturn(new ArrayList<>());
    }

    @Test
    void saveExerciseResult_Success() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(testUser));
        when(exerciseTypeService.getExerciseType(testRequest)).thenReturn(testExerciseType);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(any(User.class), any()))
                .thenReturn(100);
        when(rankService.assignRankIfEligible(any(User.class)))
                .thenReturn("");
        doNothing().when(streakService).updateStreak(any(User.class), any(LocalDate.class));
        when(unifiedAchievementService.getCompletedAchievements(any())).thenReturn(new ArrayList<>());

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        verify(userRepository, times(2)).findByTelegramId(TELEGRAM_ID); // Called twice: initial load and reload after streak update
        verify(exerciseTypeService).getExerciseType(testRequest);
        verify(notificationService).notifyFollowersAboutWorkout(testUser, testExerciseType, 10);
        verify(exerciseRecordRepository).sumTotalRepsByUserAndExerciseType(testUser, testExerciseType);
        verify(rankService).assignRankIfEligible(testUser);

        assertEquals(1, testUser.getExerciseRecords().size());
        ExerciseRecord savedExercise = testUser.getExerciseRecords().getFirst();
        assertEquals(testUser, savedExercise.getUser());
        assertEquals(testExerciseType, savedExercise.getExerciseType());
        assertEquals(10, savedExercise.getCount());
        assertEquals(LocalDate.now(), savedExercise.getDate());

        // Verify that localized message is present (Russian locale is used in test)
        assertTrue(result.contains("Отжимания"));
        assertTrue(result.contains("100"));
    }

    @Test
    void saveExerciseResult_WithRankMessage_AppendsNewLineAndRankText() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(testUser));
        when(exerciseTypeService.getExerciseType(testRequest)).thenReturn(testExerciseType);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(any(User.class), any()))
                .thenReturn(120);
        doNothing().when(streakService).updateStreak(any(User.class), any(LocalDate.class));
        when(unifiedAchievementService.getCompletedAchievements(any())).thenReturn(new ArrayList<>());

        String promotion = "\nПоздравляю! Твой ранг повышен: — → Новичок";
        when(rankService.assignRankIfEligible(any(User.class)))
                .thenReturn(promotion);

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        assertTrue(result.contains("Отжимания"));
        assertTrue(result.contains("120"));
        assertTrue(result.contains(promotion));
        verify(rankService).assignRankIfEligible(testUser);
    }

    @Test
    void saveExerciseResult_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UserNotFoundException.class, () -> exerciseService.saveExerciseResult(testRequest));

        verify(userRepository).findByTelegramId(TELEGRAM_ID);
        verifyNoInteractions(exerciseTypeRepository);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(rankService);
    }

    @Test
    void saveExerciseEntry_UnknownExerciseCode_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(testUser));
        when(exerciseTypeService.getExerciseType(any(ExerciseEntryRequest.class)))
                .thenThrow(new UnknownExerciseCodeException("unknown"));

        ExerciseEntryRequest invalidRequest = new ExerciseEntryRequest(TELEGRAM_ID, "unknown", 10);

        // When & Then
        assertThrows(
                UnknownExerciseCodeException.class,
                () -> exerciseService.saveExerciseResult(invalidRequest));

        verify(userRepository).findByTelegramId(TELEGRAM_ID);
        verify(exerciseTypeService).getExerciseType(invalidRequest);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(rankService);
    }

    @Test
    void getTotalReps_ReturnsCorrectSum() {
        // Given
        User user = new User();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, testExerciseType))
                .thenReturn(150);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(testExerciseType);

        // When
        int totalReps = exerciseService.getTotalReps(user, "push_up");

        // Then
        assertEquals(150, totalReps);
        verify(exerciseTypeService).getExerciseType("push_up");
        verify(exerciseRecordRepository).sumTotalRepsByUserAndExerciseType(user, testExerciseType);
    }

}
