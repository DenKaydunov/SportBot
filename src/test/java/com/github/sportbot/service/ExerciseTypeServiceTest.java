package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.repository.ExerciseTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
        exerciseType = ExerciseType.builder().id(1L).code("push_up").title("Отжимания").build();
        testRequest = new ExerciseEntryRequest(123456L, "push_up", 10);
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
        ExerciseEntryRequest request = new ExerciseEntryRequest(10001L, "push_up", 20);
        when(exerciseTypeRepository.findByCode("push_up")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType(request);

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("push_up");
    }

    @Test
    void getExerciseType_ByRequest_NotFound_ThrowsException() {
        // Given
        ExerciseEntryRequest request = new ExerciseEntryRequest(10001L, "unknown", 20);

        // When & Then
        assertThrows(
                UnknownExerciseCodeException.class, () -> exerciseTypeService.getExerciseType(request));

        verify(exerciseTypeRepository, never()).findByCode("unknown");
    }

    @Test
    void getExerciseType_WithRequest_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("push_up")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType(testRequest);

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("push_up");
    }

    @Test
    void getExerciseType_WithCode_Success() {
        // Given
        when(exerciseTypeRepository.findByCode("push_up")).thenReturn(Optional.of(exerciseType));

        // When
        ExerciseType result = exerciseTypeService.getExerciseType("push_up");

        // Then
        assertEquals(exerciseType, result);
        verify(exerciseTypeRepository).findByCode("push_up");
    }

}
