package com.github.sportbot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.AchievementTarget;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserExerciseTotal;
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
        when(userService.isSubscribedUser(10L)).thenReturn(true);
        when(userService.isSubscribedUser(11L)).thenReturn(true);
        when(userService.isSubscribedUser(12L)).thenReturn(false);
        when(userService.isSubscribedUser(13L)).thenReturn(true);

        when(targetsRepository.findAll()).thenReturn(List.of(
                new AchievementTarget(1L,500, null),
                new AchievementTarget(2L,1000, null),
                new AchievementTarget(3L,5000, null)
        ));

        User user1 = User.builder().telegramId(10L).fullName("Андрей").build();
        User user2 = User.builder().telegramId(11L).fullName("Алина").build();
        User user3 = User.builder().telegramId(12L).fullName("Сергей").build();
        User user4 = User.builder().telegramId(13L).fullName("Николай").build();

        ExerciseType type1 = ExerciseType.builder().code("push_up").title("Подтягивания").build();
        ExerciseType type2 = ExerciseType.builder().code("pull_up").title("Отжимания").build();


        List<UserExerciseTotal> totals = List.of(
                new UserExerciseTotal(user1, type1, 1200L),
                new UserExerciseTotal(user2, type1, 600L),
                new UserExerciseTotal(user2, type2, 1100L),
                new UserExerciseTotal(user3, type2, 6000L),
                new UserExerciseTotal(user4, type2, 400L)
        );

        when(exerciseRecordRepository.getTotalForMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totals);

        String message = achievementService.getAchievementForMonth();

        assertNotNull(message);
        assertTrue(message.contains("Подтягивания"));
        assertTrue(message.contains("Андрей"));
        assertTrue(message.contains("Алина"));
        assertFalse(message.contains("Сергей"));
        assertFalse(message.contains("Николай"));
        assertTrue(message.contains("500+: Алина"));
        assertTrue(message.contains("1000+: Андрей"));
        assertTrue(message.contains("Отжимания"));
        assertTrue(message.contains("1000+: Алина"));
    }
}