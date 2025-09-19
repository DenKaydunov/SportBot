package com.github.sportbot.service;

import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Motivation;
import com.github.sportbot.repository.MotivationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotivationServiceTest {

    @Mock
    private MotivationRepository motivationRepository;

    @InjectMocks
    private MotivationService motivationService;

    private ExerciseType testExerciseType;
    private Motivation testMotivation;

    @BeforeEach
    void setUp() {
        testExerciseType = ExerciseType.builder()
                .id(1L)
                .code("pushup")
                .title("Отжимания")
                .build();

        testMotivation = Motivation.builder()
                .id(1)
                .exerciseType(testExerciseType)
                .message("Давай, ещё немного!")
                .build();
    }

    @Test
    void getMotivation_Success() {
        // given
        when(motivationRepository.findRandomByExerciseTypeCode("pushup"))
                .thenReturn(Optional.of(testMotivation));

        // when
        String result = motivationService.getMotivation("pushup");

        // then
        assertEquals("Давай, ещё немного!", result);
        verify(motivationRepository).findRandomByExerciseTypeCode("pushup");
    }

    @Test
    void getMotivation_UnknownExerciseCode_ThrowsException() {
        // given
        when(motivationRepository.findRandomByExerciseTypeCode("invalid"))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(UnknownExerciseCodeException.class,
                () -> motivationService.getMotivation("invalid"));

        verify(motivationRepository).findRandomByExerciseTypeCode("invalid");
    }
}
