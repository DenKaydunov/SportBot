package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.StreakMilestone;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private MessageSource messageSource;

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
    private MilestoneRepository milestoneRepository;

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExerciseService exerciseService;

    private User testUser;
    private ExerciseType testExerciseType;
    private ExerciseEntryRequest testRequest;
    private static final long TELEGRAM_ID = 123456L;

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
                        .build();

        testExerciseType = ExerciseType.builder().id(1L).code("pushup").title("Отжимания").build();

        testRequest = new ExerciseEntryRequest(TELEGRAM_ID, "push_up", 10);
    }

    @Test
    void saveExerciseResult_Success() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(testUser));
        when(exerciseTypeService.getExerciseType(testRequest)).thenReturn(testExerciseType);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(any(User.class), any()))
                .thenReturn(100);
        when(rankService.assignRankIfEligible(any(User.class), any(ExerciseType.class), anyInt()))
                .thenReturn("");
        doNothing().when(streakService).updateStreak(any(User.class), any(LocalDate.class));
        doNothing().when(achievementService).checkStreakMilestones(anyLong());
        when(milestoneRepository.findAllByOrderByDaysRequiredAsc()).thenReturn(new ArrayList<>());
        when(achievementRepository.findMilestoneIdsByUserId(anyInt())).thenReturn(new ArrayList<>());
        when(userService.getUserLocale(any())).thenReturn(Locale.forLanguageTag("ru"));

        when(messageSource.getMessage(eq("workout.reps_recorded"), any(Object[].class), any()))
                .thenReturn("Отжимания: сделано 10 повторений. Общее число: 100.");

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        verify(userRepository, times(2)).findByTelegramId(TELEGRAM_ID); // Called twice: initial load and reload after streak update
        verify(exerciseTypeService).getExerciseType(testRequest);
        verify(notificationService).notifyFollowersAboutWorkout(testUser, testExerciseType, 10);
        verify(exerciseRecordRepository).sumTotalRepsByUserAndExerciseType(testUser, testExerciseType);
        verify(messageSource).getMessage(eq("workout.reps_recorded"), any(Object[].class), any());
        verify(rankService).assignRankIfEligible(testUser, testExerciseType, 100);

        assertEquals(1, testUser.getExerciseRecords().size());
        ExerciseRecord savedExercise = testUser.getExerciseRecords().getFirst();
        assertEquals(testUser, savedExercise.getUser());
        assertEquals(testExerciseType, savedExercise.getExerciseType());
        assertEquals(10, savedExercise.getCount());
        assertEquals(LocalDate.now(), savedExercise.getDate());
        assertTrue(result.contains("Отжимания: сделано 10 повторений. Общее число: 100."));
    }

    @Test
    void saveExerciseResult_WithRankMessage_AppendsNewLineAndRankText() {
        // Given
        when(userRepository.findByTelegramId(TELEGRAM_ID)).thenReturn(Optional.of(testUser));
        when(exerciseTypeService.getExerciseType(testRequest)).thenReturn(testExerciseType);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(any(User.class), any()))
                .thenReturn(120);
        doNothing().when(streakService).updateStreak(any(User.class), any(LocalDate.class));
        doNothing().when(achievementService).checkStreakMilestones(anyLong());
        when(milestoneRepository.findAllByOrderByDaysRequiredAsc()).thenReturn(new ArrayList<>());
        when(achievementRepository.findMilestoneIdsByUserId(anyInt())).thenReturn(new ArrayList<>());

        when(messageSource.getMessage(eq("workout.reps_recorded"), any(Object[].class), any()))
                .thenReturn("Отжимания: сделано 10 повторений. Общее число: 120.");

        String promotion = "\nПоздравляю! Твой ранг повышен: — → Новичок";
        when(rankService.assignRankIfEligible(any(User.class), any(ExerciseType.class), anyInt()))
                .thenReturn(promotion);

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        assertTrue(result.startsWith("Отжимания: сделано 10 повторений. Общее число: 120." + promotion));
        verify(rankService).assignRankIfEligible(testUser, testExerciseType, 120);
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
        verifyNoInteractions(messageSource);
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
        verifyNoInteractions(messageSource);
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

    @Test
    void getAchievementUpdateMessage_Milestone(){
        //Given
        User user1 = User.builder().id(10).currentStreak(10).lastWorkoutDate(LocalDate.now().minusDays(1)).build();
        User user2 = User.builder().id(11).currentStreak(20).lastWorkoutDate(LocalDate.now().minusDays(1)).build();
        User user3 = User.builder().id(12).currentStreak(50).lastWorkoutDate(LocalDate.now().minusDays(1)).build();
        User user4 = User.builder().id(13).currentStreak(51).lastWorkoutDate(LocalDate.now().minusDays(1)).build();
        User user5 = User.builder().id(14).currentStreak(22).lastWorkoutDate(LocalDate.now().minusDays(1)).build();

        StreakMilestone milestone1 = new StreakMilestone();
        milestone1.setId(1L);
        milestone1.setDaysRequired(10);
        milestone1.setTitle("Bronze streak");
        milestone1.setDescription("10 дней подряд без перерыва");
        milestone1.setRewardTon(5);

        StreakMilestone milestone2 = new StreakMilestone();
        milestone2.setId(2L);
        milestone2.setDaysRequired(20);
        milestone2.setTitle("Silver streak");
        milestone2.setDescription("20 дней стабильных тренировок");
        milestone2.setRewardTon(10);

        StreakMilestone milestone3 = new StreakMilestone();
        milestone3.setId(3L);
        milestone3.setDaysRequired(50);
        milestone3.setTitle("Gold streak");
        milestone3.setDescription("50 дней настоящей силы");
        milestone3.setRewardTon(25);

        when(milestoneRepository.findAllByOrderByDaysRequiredAsc())
                .thenReturn(List.of(milestone1, milestone2, milestone3));

        lenient().when(achievementRepository.findMilestoneIdsByUserId(11)).thenReturn(List.of());
        lenient().when(achievementRepository.findMilestoneIdsByUserId(11)).thenReturn(List.of(1L));
        lenient().when(achievementRepository.findMilestoneIdsByUserId(12)).thenReturn(List.of(1L, 2L));
        lenient().when(achievementRepository.findMilestoneIdsByUserId(13)).thenReturn(List.of(1L, 2L, 3L));
        lenient().when(achievementRepository.findMilestoneIdsByUserId(14)).thenReturn(List.of(1L, 2L));

        //When
        String result1 = ReflectionTestUtils.invokeMethod(exerciseService, "getAchievementUpdateMessage",user1);
        String result2 = ReflectionTestUtils.invokeMethod(exerciseService, "getNextAchievementUpdateMessage",user1);
        String result3 = ReflectionTestUtils.invokeMethod(exerciseService, "getAchievementUpdateMessage",user2);
        String result4 = ReflectionTestUtils.invokeMethod(exerciseService, "getNextAchievementUpdateMessage",user2);
        String result5 = ReflectionTestUtils.invokeMethod(exerciseService, "getAchievementUpdateMessage",user3);
        String result6 = ReflectionTestUtils.invokeMethod(exerciseService, "getNextAchievementUpdateMessage",user3);
        String result7 = ReflectionTestUtils.invokeMethod(exerciseService, "getAchievementUpdateMessage",user4);
        String result8 = ReflectionTestUtils.invokeMethod(exerciseService, "getNextAchievementUpdateMessage",user4);
        String result9 = ReflectionTestUtils.invokeMethod(exerciseService, "getAchievementUpdateMessage",user5);
        String result10 = ReflectionTestUtils.invokeMethod(exerciseService, "getNextAchievementUpdateMessage",user5);

        //Then
        String expected1 = "\n🏆 Поздравляем! Награда за 10 дней подряд: Bronze streak - 10 дней подряд без перерыва (Награда: 5 Ton)";
        String expected2 = "\n⏰ Тренируйся ещё 10 дней подряд для следующей награды.";
        String expected3 = "\n🏆 Поздравляем! Награда за 20 дней подряд: Silver streak - 20 дней стабильных тренировок (Награда: 10 Ton)";
        String expected4 = "\n⏰ Тренируйся ещё 30 дней подряд для следующей награды.";
        String expected5 = "\n🏆 Поздравляем! Награда за 50 дней подряд: Gold streak - 50 дней настоящей силы (Награда: 25 Ton)";
        String expected6 = "\n✅ Все награды за стрик получены!";
        String expected7 = "";
        String expected8 = "\n✅ Все награды за стрик получены!";
        String expected9 = "";
        String expected10 = "\n⏰ Тренируйся ещё 28 дней подряд для следующей награды.";

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        assertEquals(expected3, result3);
        assertEquals(expected4, result4);
        assertEquals(expected5, result5);
        assertEquals(expected6, result6);
        assertEquals(expected7, result7);
        assertEquals(expected8, result8);
        assertEquals(expected9, result9);
        assertEquals(expected10, result10);
    }
}
