package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(any(User.class), any()))
                .thenReturn(100);

        when(messageSource.getMessage(eq("workout.reps_recorded"), any(Object[].class), any()))
                .thenReturn("Отжимания: сделано 10 повторений. Общее число: 100.");

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        verify(userRepository).findByTelegramId(TELEGRAM_ID);
        verify(exerciseTypeService).getExerciseType(testRequest);
        verify(userRepository).save(testUser);
        verify(exerciseRecordRepository).sumTotalRepsByUserAndExerciseType(testUser, testExerciseType);
        verify(messageSource).getMessage(eq("workout.reps_recorded"), any(Object[].class), any());

        assertEquals(1, testUser.getExerciseRecords().size());
        ExerciseRecord savedExercise = testUser.getExerciseRecords().getFirst();
        assertEquals(testUser, savedExercise.getUser());
        assertEquals(testExerciseType, savedExercise.getExerciseType());
        assertEquals(10, savedExercise.getCount());
        assertEquals(LocalDate.now(), savedExercise.getDate());
        assertEquals("Отжимания: сделано 10 повторений. Общее число: 100.", result);
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
    }

    @Test
    void getTotalReps_ReturnsCorrectSum() {
        // Given
        User user = new User();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, testExerciseType))
                .thenReturn(150);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(testExerciseType);

        // When
        int totalReps = exerciseService.getTotalReps(user, ExerciseTypeEnum.PUSH_UP);

        // Then
        assertEquals(150, totalReps);
        verify(exerciseTypeService).getExerciseType("push_up");
        verify(exerciseRecordRepository).sumTotalRepsByUserAndExerciseType(user, testExerciseType);
    }
}
