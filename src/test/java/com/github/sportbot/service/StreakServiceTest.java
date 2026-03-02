package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StreakServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private StreakService streakService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setCurrentStreak(0);
        user.setBestStreak(0);
        user.setLastWorkoutDate(null);
    }

    @Test
    void shouldStartStreakForFirstWorkout() {
        LocalDate today = LocalDate.now();

        streakService.updateStreak(user, today);

        assertEquals(1, user.getCurrentStreak());
        assertEquals(1, user.getBestStreak());
        assertEquals(today, user.getLastWorkoutDate());

        verify(userRepository).save(user);
    }

    @Test
    void shouldNotIncreaseStreakIfWorkoutSameDay() {
        LocalDate today = LocalDate.now();

        user.setCurrentStreak(3);
        user.setBestStreak(3);
        user.setLastWorkoutDate(today);

        streakService.updateStreak(user, today);

        assertEquals(3, user.getCurrentStreak());
        assertEquals(3, user.getBestStreak());

        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldIncreaseStreakIfWorkoutYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();

        user.setCurrentStreak(2);
        user.setBestStreak(2);
        user.setLastWorkoutDate(yesterday);

        streakService.updateStreak(user, today);

        assertEquals(3, user.getCurrentStreak());
        assertEquals(3, user.getBestStreak());
        assertEquals(today, user.getLastWorkoutDate());

        verify(userRepository).save(user);
    }

    @Test
    void shouldResetStreakIfMissedMoreThanOneDay() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
        LocalDate today = LocalDate.now();

        user.setCurrentStreak(5);
        user.setBestStreak(5);
        user.setLastWorkoutDate(threeDaysAgo);

        streakService.updateStreak(user, today);

        assertEquals(1, user.getCurrentStreak());
        assertEquals(5, user.getBestStreak());
        assertEquals(today, user.getLastWorkoutDate());

        verify(userRepository).save(user);
    }

    @Test
    void shouldHandlePastWorkoutDate() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate twoDaysAgo = LocalDate.now().minusDays(2);

        user.setCurrentStreak(2);
        user.setBestStreak(2);
        user.setLastWorkoutDate(yesterday);

        when(exerciseRecordRepository.findMaxDateByUserBeforeDate(user, twoDaysAgo))
                .thenReturn(Optional.empty());

        streakService.updateStreak(user, twoDaysAgo);

        verify(userRepository).save(user);
    }


    @Test
    void shouldReturnNoWorkoutsMessage() {
        when(messageSource.getMessage(eq("streak.no_workouts"), any(), any()))
                .thenReturn("\uD83D\uDD25 Стрик: пока нет тренировок. Начни сегодня!");

        String result = streakService.getStreakInfo(user, Locale.ENGLISH);

        assertEquals("\uD83D\uDD25 Стрик: пока нет тренировок. Начни сегодня!", result);
    }

    @Test
    void shouldReturnLostStreakMessage() {
        LocalDate threeDaysAgo = LocalDate.now().minusDays(3);

        user.setLastWorkoutDate(threeDaysAgo);
        user.setBestStreak(5);

        when(messageSource.getMessage(eq("streak.lost"), any(), any()))
                .thenReturn("\uD83D\uDD25 Стрик потерян. Лучший результат: 5 дней. Прошло дней без тренировки: 3");

        String result = streakService.getStreakInfo(user, Locale.ENGLISH);

        assertEquals("\uD83D\uDD25 Стрик потерян. Лучший результат: 5 дней. Прошло дней без тренировки: 3", result);
    }

    @Test
    void shouldReturnActiveRecordMessage() {
        LocalDate today = LocalDate.now();

        user.setLastWorkoutDate(today);
        user.setCurrentStreak(5);
        user.setBestStreak(5);

        when(messageSource.getMessage(eq("streak.active_record"), any(), any()))
                .thenReturn("\uD83D\uDD25 Стрик: 5 дней подряд (новый рекорд! \uD83C\uDF89)");

        String result = streakService.getStreakInfo(user, Locale.ENGLISH);

        assertEquals("\uD83D\uDD25 Стрик: 5 дней подряд (новый рекорд! \uD83C\uDF89)", result);
    }

    @Test
    void shouldReturnActiveMessage() {
        LocalDate today = LocalDate.now();

        user.setLastWorkoutDate(today);
        user.setCurrentStreak(3);
        user.setBestStreak(5);

        when(messageSource.getMessage(eq("streak.active"), any(), any()))
                .thenReturn("\uD83D\uDD25 Стрик: 3 дней подряд (рекорд: 5 дней)");

        String result = streakService.getStreakInfo(user, Locale.ENGLISH);

        assertEquals("\uD83D\uDD25 Стрик: 3 дней подряд (рекорд: 5 дней)", result);
    }
}

