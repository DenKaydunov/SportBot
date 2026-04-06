package com.github.sportbot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.AchievementTarget;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserExerciseSummary;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.TargetsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

class AchievementAggregationServiceTest {

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @Mock
    private UserService userService;

    @Mock
    private TargetsRepository targetsRepository;

    @InjectMocks
    private AchievementAggregationService achievementService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMonthlyAchievements() {
        // Настраиваем подписки
        when(userService.isSubscribedUser(10L)).thenReturn(true);
        when(userService.isSubscribedUser(11L)).thenReturn(true);
        when(userService.isSubscribedUser(12L)).thenReturn(false);
        when(userService.isSubscribedUser(13L)).thenReturn(true);

        // Настраиваем отметки достижений
        when(targetsRepository.findAll()).thenReturn(List.of(
                new AchievementTarget(1L, 500, null),
                new AchievementTarget(2L, 1000, null),
                new AchievementTarget(3L, 5000, null)
        ));

        // Создаем пользователей
        User user1 = User.builder().telegramId(10L).fullName("Андрей").build();
        User user2 = User.builder().telegramId(11L).fullName("Алина").build();
        User user3 = User.builder().telegramId(12L).fullName("Сергей").build();
        User user4 = User.builder().telegramId(13L).fullName("Николай").build();

        ExerciseType type1 = ExerciseType.builder().id(1L).code("push_up").title("Подтягивания").build();
        ExerciseType type2 = ExerciseType.builder().id(2L).code("pull_up").title("Отжимания").build();

        // Итоги ДО начала месяца (user1: 800, user2 type1: 400, user2 type2: 900, user3: 5500, user4: 300)
        List<UserExerciseSummary> totalsBeforeMonth = List.of(
                new UserExerciseSummary(user1, type1, 800L),    // было 800
                new UserExerciseSummary(user2, type1, 400L),    // было 400
                new UserExerciseSummary(user2, type2, 900L),    // было 900
                new UserExerciseSummary(user3, type2, 5500L),   // было 5500
                new UserExerciseSummary(user4, type2, 300L)     // было 300
        );

        // Итоги ПОСЛЕ окончания месяца (user1: 1200, user2 type1: 600, user2 type2: 1100, user3: 6000, user4: 400)
        List<UserExerciseSummary> totalsAfterMonth = List.of(
                new UserExerciseSummary(user1, type1, 1200L),   // стало 1200 -> пересек 1000
                new UserExerciseSummary(user2, type1, 600L),    // стало 600 -> пересек 500
                new UserExerciseSummary(user2, type2, 1100L),   // стало 1100 -> пересек 1000
                new UserExerciseSummary(user3, type2, 6000L),   // стало 6000 -> уже был выше 5000, ничего нового
                new UserExerciseSummary(user4, type2, 400L)     // стало 400 -> не пересек ни одной отметки
        );

        // Мокаем вызовы репозитория
        when(exerciseRecordRepository.getTotalBeforeDate(any(LocalDate.class)))
                .thenReturn(totalsBeforeMonth, totalsAfterMonth);

        String message = achievementService.getAchievementForMonth();

        // Проверки
        assertNotNull(message);
        assertTrue(message.contains("Подтягивания"), "Should contain 'Подтягивания'");
        assertTrue(message.contains("Андрей"), "Should contain 'Андрей' (crossed 1000)");
        assertTrue(message.contains("Алина"), "Should contain 'Алина' (crossed 500 and 1000)");
        assertFalse(message.contains("Сергей"), "Should NOT contain 'Сергей' (not subscribed)");
        assertFalse(message.contains("Николай"), "Should NOT contain 'Николай' (didn't cross any milestone)");
        assertTrue(message.contains("500+"), "Should contain '500+' milestone");
        assertTrue(message.contains("1000+"), "Should contain '1000+' milestone");
        assertTrue(message.contains("Отжимания"), "Should contain 'Отжимания'");
    }
}