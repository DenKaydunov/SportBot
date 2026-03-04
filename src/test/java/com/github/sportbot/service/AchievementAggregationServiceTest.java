package com.github.sportbot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Thresholds;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserExerciseTotal;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.ThresholdsRepository;
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
    private ThresholdsRepository thresholdsRepository;

    @InjectMocks
    private AchievementAggregationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMonthlyAchievements() {
        // 1️⃣ Мокируем пороги
        when(thresholdsRepository.findAll()).thenReturn(List.of(
                new Thresholds(1L,500, null),
                new Thresholds(2L,1000, null),
                new Thresholds(3L,5000, null)
        ));

        User user1 = User.builder().id(10).fullName("Андрей").isSubscribed(true).build();
        User user2 = User.builder().id(10).fullName("Алина").isSubscribed(true).build();
        User user3 = User.builder().id(10).fullName("Сергей").isSubscribed(false).build();
        User user4 = User.builder().id(10).fullName("Николай").isSubscribed(true).build();

        ExerciseType type1 = ExerciseType.builder().code("push_up").title("Подтягивания").build();
        ExerciseType type2 = ExerciseType.builder().code("pull_up").title("Отжжимания").build();


        List<UserExerciseTotal> totals = List.of(
                new UserExerciseTotal(user1, type1, 1200L),
                new UserExerciseTotal(user2, type1, 600L),
                new UserExerciseTotal(user3, type2, 6000L),
                new UserExerciseTotal(user4, type2, 400L)
        );

        when(exerciseRecordRepository.getTotalForMonth(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(totals);

        String message = service.getMonthlyAchievements();

        assertNotNull(message);
        assertTrue(message.contains("Подтягивания"));
        assertTrue(message.contains("Андрей"));
        assertTrue(message.contains("Алина"));
        assertFalse(message.contains("Сергей"));
        assertFalse(message.contains("Николай"));
        assertTrue(message.contains("500+: Алина"));
        assertTrue(message.contains("1000+: Андрей"));
    }
}