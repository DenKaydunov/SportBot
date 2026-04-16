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
    private Motivation testMotivationRu;
    private Motivation testMotivationEn;

    @BeforeEach
    void setUp() {
        testExerciseType = ExerciseType.builder()
                .id(1L)
                .code("pushup")
                .title("Отжимания")
                .build();

        testMotivationRu = Motivation.builder()
                .id(1)
                .exerciseType(testExerciseType)
                .message("Давай, ещё немного!")
                .locale("ru")
                .build();

        testMotivationEn = Motivation.builder()
                .id(2)
                .exerciseType(testExerciseType)
                .message("No pain, no gain!")
                .locale("en")
                .build();
    }

    @Test
    void getMotivation_WithLocale_Success() {
        // given
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("pushup", "en"))
                .thenReturn(Optional.of(testMotivationEn));

        // when
        String result = motivationService.getMotivation("pushup", "en");

        // then
        assertEquals("No pain, no gain!", result);
        verify(motivationRepository).findRandomByExerciseTypeCodeAndLocale("pushup", "en");
    }

    @Test
    void getMotivation_WithLocale_FallbackToRu() {
        // given - no Ukrainian messages available
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("pushup", "uk"))
                .thenReturn(Optional.empty());
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("pushup", "ru"))
                .thenReturn(Optional.of(testMotivationRu));

        // when
        String result = motivationService.getMotivation("pushup", "uk");

        // then
        assertEquals("Давай, ещё немного!", result);
        verify(motivationRepository).findRandomByExerciseTypeCodeAndLocale("pushup", "uk");
        verify(motivationRepository).findRandomByExerciseTypeCodeAndLocale("pushup", "ru");
    }

    @Test
    void getMotivation_NoMessages_ThrowsException() {
        // given
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("invalid", "en"))
                .thenReturn(Optional.empty());
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("invalid", "ru"))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(UnknownExerciseCodeException.class,
                () -> motivationService.getMotivation("invalid", "en"));
    }

    @Test
    @Deprecated
    void getMotivation_LegacyMethod_DefaultsToRu() {
        // given
        when(motivationRepository.findRandomByExerciseTypeCodeAndLocale("pushup", "ru"))
                .thenReturn(Optional.of(testMotivationRu));

        // when
        String result = motivationService.getMotivation("pushup");

        // then
        assertEquals("Давай, ещё немного!", result);
    }
}
