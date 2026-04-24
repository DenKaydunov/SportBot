package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.LeaderBoardRepository;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyLeaderboardServiceTest {

    @Mock
    private SportBot sportBot;

    @Mock
    private LeaderBoardRepository leaderBoardRepository;

    @Mock
    private ExerciseTypeRepository exerciseTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @Mock
    private UserService userService;

    @InjectMocks
    private WeeklyLeaderboardService weeklyLeaderboardService;

    private User testUser;
    private ExerciseType pushUpType;
    private ExerciseType pullUpType;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setTelegramId(123456789L);
        testUser.setFullName("Test User");
        testUser.setLanguage("ru");

        pushUpType = new ExerciseType();
        pushUpType.setId(1L);
        pushUpType.setCode("push_up");
        pushUpType.setTitle("Отжимания");

        pullUpType = new ExerciseType();
        pullUpType.setId(2L);
        pullUpType.setCode("pull_up");
        pullUpType.setTitle("Подтягивания");
    }

    @Test
    void sendWeeklyCongratulations_shouldSendToAllSubscribedUsers() {
        // Given
        List<User> subscribedUsers = List.of(testUser);
        List<ExerciseType> exerciseTypes = List.of(pushUpType, pullUpType);

        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(subscribedUsers);
        when(exerciseTypeRepository.findAll()).thenReturn(exerciseTypes);
        when(userService.getUserLocale(testUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(messageSource.getMessage(eq("weekly.leaderboard.header"), any(), any()))
            .thenReturn("🏆 Топ недели (15.04.2026 - 21.04.2026) 🏆");
        when(messageSource.getMessage(eq("weekly.leaderboard.exercise.header"), any(), any()))
            .thenReturn("\n\nОтжимания:");
        when(entityLocalizationService.getExerciseTypeTitle(any(), any()))
            .thenReturn("Отжимания");

        List<Object[]> topUsers = new ArrayList<>();
        topUsers.add(new Object[]{"Иван", 1000L});
        topUsers.add(new Object[]{"Петр", 850L});
        topUsers.add(new Object[]{"Мария", 720L});

        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), eq(3), any(), any()))
            .thenReturn(topUsers);
        when(messageSource.getMessage(eq("weekly.leaderboard.place"), any(), any()))
            .thenAnswer(invocation -> {
                Object[] args = invocation.getArgument(1);
                return args[0] + ". " + args[1] + " - " + args[2];
            });

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        verify(userRepository).findAllByIsSubscribedTrue();
        verify(sportBot).sendTgMessage(eq(testUser.getTelegramId()), anyString());
        verify(exerciseTypeRepository).findAll();
    }

    @Test
    void sendWeeklyCongratulations_whenNoSubscribedUsers_shouldNotSendMessages() {
        // Given
        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(List.of());

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        verify(userRepository).findAllByIsSubscribedTrue();
        verify(sportBot, never()).sendTgMessage(anyLong(), anyString());
    }

    @Test
    void sendWeeklyCongratulations_whenNoRecords_shouldSendEmptyMessage() {
        // Given
        List<User> subscribedUsers = List.of(testUser);
        List<ExerciseType> exerciseTypes = List.of(pushUpType);

        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(subscribedUsers);
        when(exerciseTypeRepository.findAll()).thenReturn(exerciseTypes);
        when(userService.getUserLocale(testUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(messageSource.getMessage(eq("weekly.leaderboard.header"), any(), any()))
            .thenReturn("🏆 Топ недели (15.04.2026 - 21.04.2026) 🏆");
        when(messageSource.getMessage(eq("weekly.leaderboard.no.records"), isNull(), any()))
            .thenReturn("Нет записей за неделю");

        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), eq(3), any(), any()))
            .thenReturn(List.of());

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sportBot).sendTgMessage(eq(testUser.getTelegramId()), messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertThat(message).contains("Нет записей за неделю");
    }

    @Test
    void sendWeeklyCongratulations_whenExceptionOccurs_shouldContinueWithOtherUsers() {
        // Given
        User user1 = new User();
        user1.setTelegramId(111L);
        user1.setFullName("User 1");
        user1.setLanguage("ru");

        User user2 = new User();
        user2.setTelegramId(222L);
        user2.setFullName("User 2");
        user2.setLanguage("ru");

        List<User> subscribedUsers = List.of(user1, user2);
        List<ExerciseType> exerciseTypes = List.of(pushUpType);

        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(subscribedUsers);
        when(exerciseTypeRepository.findAll()).thenReturn(exerciseTypes);
        when(userService.getUserLocale(any())).thenReturn(Locale.forLanguageTag("ru"));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Test message");
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), eq(3), any(), any()))
            .thenReturn(List.of());

        doThrow(new RuntimeException("Send failed"))
            .when(sportBot).sendTgMessage(eq(user1.getTelegramId()), anyString());

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        verify(sportBot).sendTgMessage(eq(user1.getTelegramId()), anyString());
        verify(sportBot).sendTgMessage(eq(user2.getTelegramId()), anyString());
    }

    @Test
    void sendWeeklyCongratulations_shouldCalculateCorrectPreviousWeekRange() {
        // Given
        List<User> subscribedUsers = List.of(testUser);
        List<ExerciseType> exerciseTypes = List.of(pushUpType);

        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(subscribedUsers);
        when(exerciseTypeRepository.findAll()).thenReturn(exerciseTypes);
        when(userService.getUserLocale(testUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Test");
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(anyLong(), isNull(), eq(3), any(), any()))
            .thenReturn(List.of());

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        ArgumentCaptor<LocalDate> startDateCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endDateCaptor = ArgumentCaptor.forClass(LocalDate.class);

        verify(leaderBoardRepository).findTopUsersByExerciseTypeAndDate(
            anyLong(),
            isNull(),
            eq(3),
            startDateCaptor.capture(),
            endDateCaptor.capture()
        );

        LocalDate startDate = startDateCaptor.getValue();
        LocalDate endDate = endDateCaptor.getValue();

        // Verify it's Monday-Sunday range
        assertThat(startDate.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(endDate.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        // Verify it's 7 days apart
        assertThat(java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate)).isEqualTo(6);

        // Verify it's in the past (before today)
        LocalDate now = LocalDate.now();
        assertThat(endDate).isBefore(now);
    }

    @Test
    void sendWeeklyCongratulations_shouldIncludeTop3ForEachExercise() {
        // Given
        List<User> subscribedUsers = List.of(testUser);
        List<ExerciseType> exerciseTypes = List.of(pushUpType, pullUpType);

        when(userRepository.findAllByIsSubscribedTrue()).thenReturn(subscribedUsers);
        when(exerciseTypeRepository.findAll()).thenReturn(exerciseTypes);
        when(userService.getUserLocale(testUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(messageSource.getMessage(eq("weekly.leaderboard.header"), any(), any()))
            .thenReturn("🏆 Топ недели 🏆");
        when(messageSource.getMessage(eq("weekly.leaderboard.exercise.header"), any(), any()))
            .thenAnswer(invocation -> "\n\n" + invocation.getArgument(1, Object[].class)[0] + ":");
        when(entityLocalizationService.getExerciseTypeTitle(pushUpType, Locale.forLanguageTag("ru")))
            .thenReturn("Отжимания");
        when(entityLocalizationService.getExerciseTypeTitle(pullUpType, Locale.forLanguageTag("ru")))
            .thenReturn("Подтягивания");

        List<Object[]> pushUpTop = List.of(
            new Object[]{"User1", 100L},
            new Object[]{"User2", 90L},
            new Object[]{"User3", 80L}
        );

        List<Object[]> pullUpTop = List.of(
            new Object[]{"User4", 50L},
            new Object[]{"User5", 45L}
        );

        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(eq(1L), isNull(), eq(3), any(), any()))
            .thenReturn(pushUpTop);
        when(leaderBoardRepository.findTopUsersByExerciseTypeAndDate(eq(2L), isNull(), eq(3), any(), any()))
            .thenReturn(pullUpTop);

        when(messageSource.getMessage(eq("weekly.leaderboard.place"), any(), any()))
            .thenAnswer(invocation -> {
                Object[] args = invocation.getArgument(1);
                return args[0] + ". " + args[1] + " - " + args[2];
            });

        // When
        weeklyLeaderboardService.sendWeeklyCongratulations();

        // Then
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sportBot).sendTgMessage(eq(testUser.getTelegramId()), messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertThat(message).contains("Отжимания:");
        assertThat(message).contains("Подтягивания:");
        assertThat(message).contains("User1");
        assertThat(message).contains("User4");
    }
}
