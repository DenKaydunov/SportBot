package com.github.sportbot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageLocalizerImplTest {

    @Mock
    private MessageSource messageSource;

    private MessageLocalizerImpl messageLocalizer;

    @BeforeEach
    void setUp() {
        messageLocalizer = new MessageLocalizerImpl(messageSource);
    }

    @Test
    void localize_withoutContext_shouldReturnLocalizedMessage() {
        // Given
        String messageKey = "greeting.message";
        Locale locale = Locale.forLanguageTag("ru");
        String expectedMessage = "Привет!";

        when(messageSource.getMessage(messageKey, null, locale))
                .thenReturn(expectedMessage);

        // When
        String result = messageLocalizer.localize(messageKey, null, locale);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
        verify(messageSource).getMessage(messageKey, null, locale);
    }

    @Test
    void localize_withContext_shouldReturnFormattedMessage() {
        // Given
        String messageKey = "welcome.user";
        Object[] context = new Object[]{"John", 100};
        Locale locale = Locale.forLanguageTag("en");
        String expectedMessage = "Welcome, John! You have 100 points.";

        when(messageSource.getMessage(messageKey, context, locale))
                .thenReturn(expectedMessage);

        // When
        String result = messageLocalizer.localize(messageKey, context, locale);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
        verify(messageSource).getMessage(messageKey, context, locale);
    }

    @Test
    void localize_withRussianLocale_shouldReturnRussianMessage() {
        // Given
        String messageKey = "exercise.saved";
        Object[] context = new Object[]{"Отжимания", 50};
        Locale ruLocale = Locale.forLanguageTag("ru");
        String expectedMessage = "Отжимания: сохранено 50 повторений";

        when(messageSource.getMessage(messageKey, context, ruLocale))
                .thenReturn(expectedMessage);

        // When
        String result = messageLocalizer.localize(messageKey, context, ruLocale);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void localize_withEnglishLocale_shouldReturnEnglishMessage() {
        // Given
        String messageKey = "exercise.saved";
        Object[] context = new Object[]{"Push-ups", 50};
        Locale enLocale = Locale.forLanguageTag("en");
        String expectedMessage = "Push-ups: saved 50 reps";

        when(messageSource.getMessage(messageKey, context, enLocale))
                .thenReturn(expectedMessage);

        // When
        String result = messageLocalizer.localize(messageKey, context, enLocale);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }

    @Test
    void localize_withEmptyContext_shouldWork() {
        // Given
        String messageKey = "error.message";
        Object[] emptyContext = new Object[]{};
        Locale locale = Locale.forLanguageTag("ru");
        String expectedMessage = "Произошла ошибка";

        when(messageSource.getMessage(messageKey, emptyContext, locale))
                .thenReturn(expectedMessage);

        // When
        String result = messageLocalizer.localize(messageKey, emptyContext, locale);

        // Then
        assertThat(result).isEqualTo(expectedMessage);
    }
}
