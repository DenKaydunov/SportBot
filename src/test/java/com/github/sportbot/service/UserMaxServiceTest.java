package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserMaxHistory;
import com.github.sportbot.repository.UserRepository;
import com.github.sportbot.repository.WorkoutHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMaxServiceTest {

    @Mock
    private UserProgramService userProgramService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ExerciseService exerciseService;
    @Mock
    private WorkoutHistoryRepository workoutHistoryRepository;
    @Mock
    private MessageSource mSource;

    @InjectMocks
    private UserMaxService userMaxService;

    private User testUser;
    private ExerciseType testExerciseType;
    private ExerciseEntryRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .telegramId(123456)
                .isSubscribed(true)
                .workoutHistory(new ArrayList<>())
                .maxHistory(new ArrayList<>())
                .build();

        testExerciseType = ExerciseType.builder()
                .id(1L)
                .code("pushup")
                .title("Отжимания")
                .build();

        testRequest = new ExerciseEntryRequest(123456, "pushup", 10);
    }



    @Test
    void saveExerciseMaxResult_Success() {
        // Given
        when(userService.getUserByTelegramId(123456)).thenReturn(testUser);
        when(exerciseService.getExerciseType(any(ExerciseEntryRequest.class))).thenReturn(testExerciseType);
        final int totalCount = 100;
        when(workoutHistoryRepository.sumTotalReps(testUser, testExerciseType)).thenReturn(totalCount);

        // When
        userMaxService.saveExerciseMaxResult(testRequest);

        // Then
        verify(userService).getUserByTelegramId(123456);
        verify(userProgramService).updateProgram(testUser, testExerciseType, 10);
        verify(userRepository).save(testUser);

        assertEquals(1, testUser.getMaxHistory().size());
        UserMaxHistory savedMax = testUser.getMaxHistory().getFirst();
        assertEquals(testUser, savedMax.getUser());
        assertEquals(testExerciseType, savedMax.getExerciseType());
        assertEquals(10, savedMax.getMaxValue());
        assertEquals(LocalDate.now(), savedMax.getDate());
        verify(mSource).getMessage(
                eq("workout.max_reps"),
                any(Object[].class),
                eq(Locale.forLanguageTag("ru-RU"))
        );
    }


    @Test
    void saveExerciseMaxResult_UserNotFound_ThrowsException() {
        // Given
        when(userService.getUserByTelegramId(123456)).thenThrow(new UserNotFoundException());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userMaxService.saveExerciseMaxResult(testRequest));

        verify(userService).getUserByTelegramId(123456);
        verifyNoInteractions(exerciseService);
        verifyNoInteractions(userProgramService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(workoutHistoryRepository);
        verifyNoInteractions(mSource);
    }

    @Test
    void saveExerciseMaxResult_UnknownExerciseCode_ThrowsException() {
        // Given
        when(userService.getUserByTelegramId(123456)).thenReturn(testUser);
        when(exerciseService.getExerciseType(any(ExerciseEntryRequest.class))).thenThrow(new UnknownExerciseCodeException());

        ExerciseEntryRequest invalidRequest = new ExerciseEntryRequest(123456, "unknown", 10);

        // When & Then
        assertThrows(UnknownExerciseCodeException.class, () -> userMaxService.saveExerciseMaxResult(invalidRequest));

        verify(userService).getUserByTelegramId(123456);
        verify(exerciseService).getExerciseType(any(ExerciseEntryRequest.class));
        verifyNoInteractions(userProgramService);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(workoutHistoryRepository);
        verifyNoInteractions(mSource);
    }
}
