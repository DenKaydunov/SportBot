package com.github.sportbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class MessageLocalizerIntegrationTest {

    @Autowired
    private MessageLocalizer messageLocalizer;

    @ParameterizedTest
    @ValueSource(strings = {"ru", "en", "uk"})
    void testLocalizeSimpleMessageInAllLanguages(String language) {
        // Given
        Locale locale = Locale.forLanguageTag(language);
        String messageKey = "user.registered";

        // When
        String result = messageLocalizer.localize(messageKey, null, locale);

        // Then
        assertThat(result)
            .isNotEmpty()
            .doesNotContain(messageKey); // Should not return the key itself

        // Verify language-specific content
        switch (language) {
            case "ru" -> assertThat(result).contains("зарегистрирован");
            case "en" -> assertThat(result).contains("registered");
            case "uk" -> assertThat(result).contains("зареєстрован");
            default -> throw new IllegalArgumentException("Unexpected language: " + language);
        }
    }

    @Test
    void testLocalizeMessageWithSingleParameter() {
        // Given
        String messageKey = "profile.updated";
        Object[] context = new Object[]{"John Doe"};
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");

        // When
        String ruResult = messageLocalizer.localize(messageKey, context, ruLocale);
        String enResult = messageLocalizer.localize(messageKey, context, enLocale);

        // Then
        assertThat(ruResult)
            .contains("John Doe")
            .contains("обновлён");
        assertThat(enResult)
            .contains("John Doe")
            .contains("updated");
    }

    @Test
    void testLocalizeMessageWithMultipleParameters() {
        // Given
        String messageKey = "workout.today_sets";
        Object[] context = new Object[]{"5 x 20", "100"};
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");

        // When
        String ruResult = messageLocalizer.localize(messageKey, context, ruLocale);
        String enResult = messageLocalizer.localize(messageKey, context, enLocale);

        // Then
        assertThat(ruResult)
            .contains("5 x 20")
            .contains("100");
        assertThat(enResult)
            .contains("5 x 20")
            .contains("100");
    }

    @Test
    void testLocalizeComplexMessageWithFourParameters() {
        // Given
        String messageKey = "workout.max_reps";
        Object[] context = new Object[]{"Push-ups", "Ivan", "50", "200"};
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When
        String ruResult = messageLocalizer.localize(messageKey, context, ruLocale);
        String enResult = messageLocalizer.localize(messageKey, context, enLocale);
        String ukResult = messageLocalizer.localize(messageKey, context, ukLocale);

        // Then
        // Verify all parameters are present
        for (String result : new String[]{ruResult, enResult, ukResult}) {
            assertThat(result).contains("Push-ups");
            assertThat(result).contains("Ivan");
            assertThat(result).contains("50");
            assertThat(result).contains("200");
        }

        // Verify emojis are preserved
        assertThat(ruResult)
            .contains("🔥")
            .contains("💪");
        assertThat(enResult).contains("🔥");
        assertThat(ukResult).contains("🔥");

        // Verify language-specific words
        assertThat(ruResult).contains("Поздравляю");
        assertThat(enResult).contains("Congratulations");
    }

    @Test
    void testLocalizeStreakMessages() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");

        // When & Then - streak.no_workouts
        String ruNoWorkouts = messageLocalizer.localize("streak.no_workouts", null, ruLocale);
        String enNoWorkouts = messageLocalizer.localize("streak.no_workouts", null, enLocale);

        assertThat(ruNoWorkouts)
            .contains("🔥")
            .contains("Стрик");
        assertThat(enNoWorkouts)
            .contains("🔥")
            .contains("Streak");

        // When & Then - streak.active
        Object[] activeContext = new Object[]{"5", "10"};
        String ruActive = messageLocalizer.localize("streak.active", activeContext, ruLocale);
        String enActive = messageLocalizer.localize("streak.active", activeContext, enLocale);

        assertThat(ruActive)
            .contains("5")
            .contains("10");
        assertThat(enActive)
            .contains("5")
            .contains("10");

        // When & Then - streak.active_record
        Object[] recordContext = new Object[]{"15"};
        String ruRecord = messageLocalizer.localize("streak.active_record", recordContext, ruLocale);
        String enRecord = messageLocalizer.localize("streak.active_record", recordContext, enLocale);

        assertThat(ruRecord)
            .contains("15")
            .contains("🎉");
        assertThat(enRecord)
            .contains("15")
            .contains("🎉");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ru", "en", "uk"})
    void testAllLanguagesForKeyWorkoutKeys(String language) {
        // Given
        Locale locale = Locale.forLanguageTag(language);

        // When & Then - workout.reps_recorded
        Object[] repsContext = new Object[]{"Push-ups", "50", "200"};
        String repsResult = messageLocalizer.localize("workout.reps_recorded", repsContext, locale);
        assertThat(repsResult)
            .isNotEmpty()
            .contains("Push-ups")
            .contains("50")
            .contains("200");

        // When & Then - workout.rank_promoted
        Object[] rankContext = new Object[]{"Seed", "Sprout"};
        String rankResult = messageLocalizer.localize("workout.rank_promoted", rankContext, locale);
        assertThat(rankResult)
            .isNotEmpty()
            .contains("Seed")
            .contains("Sprout")
            .contains("→");

        // When & Then - workout.streak_updated
        Object[] streakContext = new Object[]{"7"};
        String streakResult = messageLocalizer.localize("workout.streak_updated", streakContext, locale);
        assertThat(streakResult)
            .isNotEmpty()
            .contains("🔥")
            .contains("7");
    }

    @Test
    void testValidKeyReturnsLocalizedMessage() {
        // Given
        Locale locale = Locale.forLanguageTag("en");

        // When
        // MessageLocalizer delegates to MessageSource which uses getMessage(key, args, defaultMessage, locale)
        // The EntityLocalizationService uses fallback, but MessageLocalizer itself doesn't have fallback parameter
        // So we test that a real key works, and document that missing keys would throw exception
        String realKey = "user.registered";
        String result = messageLocalizer.localize(realKey, null, locale);

        // Then
        assertThat(result)
            .isNotEmpty()
            .contains("registered");

        // Note: MessageLocalizer doesn't provide fallback mechanism in its interface
        // Missing keys would throw NoSuchMessageException from MessageSource
        // This is expected behavior - callers should handle exceptions or use keys that exist
    }

    @Test
    void testUkrainianLanguageSpecificContent() {
        // Given
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When
        String registered = messageLocalizer.localize("user.registered", null, ukLocale);
        String workoutReps = messageLocalizer.localize(
            "workout.reps_recorded",
            new Object[]{"Віджимання", "25", "100"},
            ukLocale
        );

        // Then
        assertThat(registered).isNotEmpty();
        assertThat(workoutReps)
            .contains("Віджимання")
            .contains("25")
            .contains("100");
    }

    @Test
    void testRussianCyrillicEncoding() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");

        // When
        String registered = messageLocalizer.localize("user.registered", null, ruLocale);
        String errorNotFound = messageLocalizer.localize("error.user.not.found", null, ruLocale);

        // Then - Verify Cyrillic characters are properly encoded
        assertThat(registered).contains("зарегистрирован");
        assertThat(errorNotFound)
            .contains("пользователь")
            .contains("не найден");
    }
}
