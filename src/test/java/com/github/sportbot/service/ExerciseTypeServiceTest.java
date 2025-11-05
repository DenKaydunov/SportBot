package com.github.sportbot.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.repository.ExerciseTypeRepository;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExerciseTypeServiceTest {

    @Mock
    private ExerciseTypeRepository exerciseTypeRepository;

    @InjectMocks
    private ExerciseTypeService exerciseTypeService;

    private ExerciseType exerciseType;

    private ExerciseEntryRequest testRequest;

    @BeforeEach
    void setUp() {
        exerciseType = ExerciseType.builder().id(1L).code("pushups").title("Отжимания").build();
        testRequest = new ExerciseEntryRequest(123456, "pushup", 10);
    }

    @Test
    void getExerciseType_ByCode_NotFound_ThrowsException() {
        // Given
        when(exerciseTypeRepository.findByCode("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UnknownExerciseCodeException.class, () -> exerciseTypeService.getExerciseType("unknown"));

        verify(exerciseTypeRepository).findByCode("unknown");
    }

    @Test
    void getExerciseType_ByRequest_Success() {
        // Given
        ExerciseEntryRequest request = new ExerciseEntryRequest(10001, "pushups", 20);
        when(exerciseTypeRepository.findByCode("pushups")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType(request);

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("pushups");
    }

    @Test
    void getExerciseType_ByRequest_NotFound_ThrowsException() {
        // Given
        ExerciseEntryRequest request = new ExerciseEntryRequest(10001, "unknown", 20);
        when(exerciseTypeRepository.findByCode("unknown")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UnknownExerciseCodeException.class, () -> exerciseTypeService.getExerciseType(request));

        verify(exerciseTypeRepository).findByCode("unknown");
    }

    @Test
    void getExerciseType_WithRequest_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("pushup")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType(testRequest);

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("pushup");
    }

    @Test
    void getExerciseType_WithCode_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("pushup")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType("pushup");

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("pushup");
    }

}
