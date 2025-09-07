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

    @InjectMocks
    private ExerciseService exerciseService;

    private User testUser;
    private ExerciseType testExerciseType;
    private ExerciseEntryRequest testRequest;


    @Mock private UserProgramRepository userProgramRepository;
    @Mock private UserMaxHistoryRepository userMaxHistoryRepository;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .telegramId(123456)
                .isSubscribed(true)
                .exerciseRecord(new ArrayList<>())
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
    void saveExerciseResult_Success() {
        // Given
        when(userRepository.findByTelegramId(123456)).thenReturn(Optional.of(testUser));
        when(exerciseTypeRepository.findByCode("pushup")).thenReturn(Optional.of(testExerciseType));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(exerciseRecordRepository.sumTotalReps(any(User.class), any(ExerciseType.class))).thenReturn(100);

        when(messageSource.getMessage(
                eq("workout.reps_recorded"),
                any(Object[].class),
                any())
        ).thenReturn("Отжимания: сделано 10 повторений. Общее число: 100.");

        // When
        String result = exerciseService.saveExerciseResult(testRequest);

        // Then
        verify(userRepository).findByTelegramId(123456);
        verify(exerciseTypeRepository).findByCode("pushup");
        verify(userRepository).save(testUser);
        verify(exerciseRecordRepository).sumTotalReps(testUser, testExerciseType);
        verify(messageSource).getMessage(
                eq("workout.reps_recorded"),
                any(Object[].class),
                any());
        
        assertEquals(1, testUser.getExerciseRecord().size());
        ExerciseRecord savedExercise = testUser.getExerciseRecord().getFirst();
        assertEquals(testUser, savedExercise.getUser());
        assertEquals(testExerciseType, savedExercise.getExerciseType());
        assertEquals(10, savedExercise.getCount());
        assertEquals(LocalDate.now(), savedExercise.getDate());
        assertEquals("Отжимания: сделано 10 повторений. Общее число: 100.", result);
    }

    @Test
    void saveExerciseResult_UserNotFound_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(123456)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> exerciseService.saveExerciseResult(testRequest));

        verify(userRepository).findByTelegramId(123456);
        verifyNoInteractions(exerciseTypeRepository);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(messageSource);
    }

    @Test
    void saveExerciseEntry_UnknownExerciseCode_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(123456)).thenReturn(Optional.of(testUser));
        when(exerciseTypeRepository.findByCode("unknown")).thenReturn(Optional.empty());

        ExerciseEntryRequest invalidRequest = new ExerciseEntryRequest(123456, "unknown", 10);

        // When & Then
        assertThrows(UnknownExerciseCodeException.class, () -> exerciseService.saveExerciseResult(invalidRequest));

        verify(userRepository).findByTelegramId(123456);
        verify(exerciseTypeRepository).findByCode("unknown");
        verify(userRepository, never()).save(any());
        verifyNoInteractions(messageSource);
    }

    @Test
    void getExerciseType_WithRequest_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("pushup")).thenReturn(Optional.of(testExerciseType));

        // When
        ExerciseType result = exerciseService.getExerciseType(testRequest);

        // Then
        assertEquals(testExerciseType, result);
        verify(exerciseTypeRepository).findByCode("pushup");
    }

    @Test
    void getExerciseType_WithCode_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("pushup")).thenReturn(Optional.of(testExerciseType));

        // When
        ExerciseType result = exerciseService.getExerciseType("pushup");

        // Then
        assertEquals(testExerciseType, result);
        verify(exerciseTypeRepository).findByCode("pushup");
    }

    @Test
    void getExerciseType_UnknownCode_ThrowsException() {
        // Given
        when(exerciseTypeRepository.findByCode("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UnknownExerciseCodeException.class, () -> exerciseService.getExerciseType("unknown"));

        verify(exerciseTypeRepository).findByCode("unknown");
    }
}
