package com.github.sportbot.service;

import com.github.sportbot.config.WorkoutProperties;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.UserProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProgramServiceTest {

    @Mock
    private UserProgramRepository userProgramRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private WorkoutProperties workoutProperties;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private UserService userService;

    @Mock
    private UserMaxService userMaxService;

    @InjectMocks
    private UserProgramService userProgramService;

    private User user;
    private ExerciseType exerciseType;
    private UserProgram existingProgram;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1)
                .telegramId(12345L)
                .fullName("John Doe")
                .build();

        exerciseType = ExerciseType.builder()
                .id(10L)
                .code("push_up")
                .build();

        existingProgram = UserProgram.builder()
                .id(new UserProgramId(user.getId(), exerciseType.getId()))
                .user(user)
                .exerciseType(exerciseType)
                .currentMax(50)
                .dayNumber(1)
                .build();
    }

    @Test
    void getWorkoutPlan_Success() {
        // Given
        when(userService.getUserByTelegramId(user.getTelegramId())).thenReturn(user);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId()))
                .thenReturn(Optional.of(existingProgram));
        when(userMaxService.getLastMax(user, exerciseType)).thenReturn(50);

        when(workoutProperties.getIncrementPerDay()).thenReturn(0.05);
        when(workoutProperties.getCoefficients()).thenReturn(List.of(0.5, 0.7, 0.9));

        when(messageSource.getMessage(
                eq("workout.today_sets"),
                any(),
                eq(Locale.forLanguageTag("ru-RU"))
        )).thenReturn("Сегодня твоя тренировка: 25, 35, 45 — всего 105");

        // When
        WorkoutPlanResponse response = userProgramService.getWorkoutPlan(user.getTelegramId(), "push_up");

        // Then
        assertNotNull(response);
        assertEquals(List.of(25, 35, 45), response.sets());
        assertEquals(105, response.totalReps());
        assertEquals("Сегодня твоя тренировка: 25, 35, 45 — всего 105", response.message());

        verify(userService).getUserByTelegramId(user.getTelegramId());
        verify(exerciseTypeService).getExerciseType("push_up");
        verify(userProgramRepository).findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId());
        verify(userMaxService).getLastMax(user, exerciseType);
        verify(messageSource).getMessage(eq("workout.today_sets"), any(), eq(Locale.forLanguageTag("ru-RU")));
    }

    @Test
    void incrementDayProgram_Success() {
        // Given
        when(userService.getUserByTelegramId(user.getTelegramId())).thenReturn(user);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId()))
                .thenReturn(Optional.of(existingProgram));

        // When
        userProgramService.incrementDayProgram(user.getTelegramId(), "push_up");

        // Then
        assertEquals(2, existingProgram.getDayNumber());
        verify(userProgramRepository, times(2)).save(existingProgram);
    }

    @Test
    void getWorkoutPlan_ResetsProgram_WhenMaxChanged() {
        // Given
        when(userService.getUserByTelegramId(user.getTelegramId())).thenReturn(user);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(exerciseType);
        when(userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId()))
                .thenReturn(Optional.of(existingProgram));
        when(userMaxService.getLastMax(user, exerciseType)).thenReturn(60); // Новый максимум

        when(workoutProperties.getIncrementPerDay()).thenReturn(0.05);
        when(workoutProperties.getCoefficients()).thenReturn(List.of(0.5, 0.7, 0.9));

        when(messageSource.getMessage(any(), any(), any())).thenReturn("Сообщение");

        // When
        WorkoutPlanResponse response = userProgramService.getWorkoutPlan(user.getTelegramId(), "push_up");

        // Then
        assertNotNull(response);
        verify(userProgramRepository).save(any(UserProgram.class)); // программа пересохраняется
        assertEquals(60, userMaxService.getLastMax(user, exerciseType));
    }
}
