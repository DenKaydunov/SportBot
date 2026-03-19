package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.model.StreakMilestone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityLocalizationServiceTest {

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private EntityLocalizationService entityLocalizationService;

    private Locale ruLocale;
    private Locale enLocale;
    private Locale ukLocale;

    @BeforeEach
    void setUp() {
        ruLocale = Locale.forLanguageTag("ru");
        enLocale = Locale.forLanguageTag("en");
        ukLocale = Locale.forLanguageTag("uk");
    }

    @Test
    void getExerciseTypeTitle_ReturnsLocalizedTitle() {
        // Given
        ExerciseType exerciseType = ExerciseType.builder()
                .code("push_up")
                .title("Отжимания")
                .build();

        when(messageSource.getMessage(eq("exercise.type.push_up"), isNull(), eq("Отжимания"), eq(ruLocale)))
                .thenReturn("Отжимания");
        when(messageSource.getMessage(eq("exercise.type.push_up"), isNull(), eq("Отжимания"), eq(enLocale)))
                .thenReturn("Push-ups");
        when(messageSource.getMessage(eq("exercise.type.push_up"), isNull(), eq("Отжимания"), eq(ukLocale)))
                .thenReturn("Віджимання");

        // When & Then
        assertEquals("Отжимания", entityLocalizationService.getExerciseTypeTitle(exerciseType, ruLocale));
        assertEquals("Push-ups", entityLocalizationService.getExerciseTypeTitle(exerciseType, enLocale));
        assertEquals("Віджимання", entityLocalizationService.getExerciseTypeTitle(exerciseType, ukLocale));
    }

    @Test
    void getExerciseTypeTitle_MissingKey_ReturnsFallback() {
        // Given
        ExerciseType exerciseType = ExerciseType.builder()
                .code("unknown_exercise")
                .title("Неизвестное упражнение")
                .build();

        // MessageSource returns fallback when key not found
        when(messageSource.getMessage(anyString(), isNull(), eq("Неизвестное упражнение"), any(Locale.class)))
                .thenReturn("Неизвестное упражнение");

        // When
        String result = entityLocalizationService.getExerciseTypeTitle(exerciseType, ruLocale);

        // Then
        assertEquals("Неизвестное упражнение", result);
    }

    @Test
    void getRankTitle_ReturnsLocalizedTitle() {
        // Given
        Rank rank = Rank.builder()
                .code("john_wick")
                .title("Джон Уик")
                .build();

        when(messageSource.getMessage(eq("rank.john_wick"), isNull(), eq("Джон Уик"), eq(ruLocale)))
                .thenReturn("Джон Уик");
        when(messageSource.getMessage(eq("rank.john_wick"), isNull(), eq("Джон Уик"), eq(enLocale)))
                .thenReturn("John Wick");
        when(messageSource.getMessage(eq("rank.john_wick"), isNull(), eq("Джон Уик"), eq(ukLocale)))
                .thenReturn("Джон Вік");

        // When & Then
        assertEquals("Джон Уик", entityLocalizationService.getRankTitle(rank, ruLocale));
        assertEquals("John Wick", entityLocalizationService.getRankTitle(rank, enLocale));
        assertEquals("Джон Вік", entityLocalizationService.getRankTitle(rank, ukLocale));
    }

    @Test
    void getRankTitle_MissingKey_ReturnsFallback() {
        // Given
        Rank rank = Rank.builder()
                .code("unknown_rank")
                .title("Неизвестный ранг")
                .build();

        when(messageSource.getMessage(anyString(), isNull(), eq("Неизвестный ранг"), any(Locale.class)))
                .thenReturn("Неизвестный ранг");

        // When
        String result = entityLocalizationService.getRankTitle(rank, enLocale);

        // Then
        assertEquals("Неизвестный ранг", result);
    }

    @Test
    void getStreakMilestoneTitle_ReturnsLocalizedTitle() {
        // Given
        StreakMilestone milestone = new StreakMilestone();
        milestone.setId(1L);
        milestone.setTitle("Bronze streak");

        when(messageSource.getMessage(eq("streak.milestone.1.title"), isNull(), eq("Bronze streak"), eq(ruLocale)))
                .thenReturn("Бронзовый стрик");
        when(messageSource.getMessage(eq("streak.milestone.1.title"), isNull(), eq("Bronze streak"), eq(enLocale)))
                .thenReturn("Bronze Streak");
        when(messageSource.getMessage(eq("streak.milestone.1.title"), isNull(), eq("Bronze streak"), eq(ukLocale)))
                .thenReturn("Бронзовий стрік");

        // When & Then
        assertEquals("Бронзовый стрик", entityLocalizationService.getStreakMilestoneTitle(milestone, ruLocale));
        assertEquals("Bronze Streak", entityLocalizationService.getStreakMilestoneTitle(milestone, enLocale));
        assertEquals("Бронзовий стрік", entityLocalizationService.getStreakMilestoneTitle(milestone, ukLocale));
    }

    @Test
    void getStreakMilestoneDescription_ReturnsLocalizedDescription() {
        // Given
        StreakMilestone milestone = new StreakMilestone();
        milestone.setId(2L);
        milestone.setDescription("20 дней стабильных тренировок");

        when(messageSource.getMessage(eq("streak.milestone.2.description"), isNull(), eq("20 дней стабильных тренировок"), eq(ruLocale)))
                .thenReturn("20 дней стабильных тренировок");
        when(messageSource.getMessage(eq("streak.milestone.2.description"), isNull(), eq("20 дней стабильных тренировок"), eq(enLocale)))
                .thenReturn("20 days of consistent training");
        when(messageSource.getMessage(eq("streak.milestone.2.description"), isNull(), eq("20 дней стабильных тренировок"), eq(ukLocale)))
                .thenReturn("20 днів стабільних тренувань");

        // When & Then
        assertEquals("20 дней стабильных тренировок", entityLocalizationService.getStreakMilestoneDescription(milestone, ruLocale));
        assertEquals("20 days of consistent training", entityLocalizationService.getStreakMilestoneDescription(milestone, enLocale));
        assertEquals("20 днів стабільних тренувань", entityLocalizationService.getStreakMilestoneDescription(milestone, ukLocale));
    }

    @Test
    void getStreakMilestoneDescription_MissingKey_ReturnsFallback() {
        // Given
        StreakMilestone milestone = new StreakMilestone();
        milestone.setId(999L);
        milestone.setDescription("Unknown milestone description");

        when(messageSource.getMessage(anyString(), isNull(), eq("Unknown milestone description"), any(Locale.class)))
                .thenReturn("Unknown milestone description");

        // When
        String result = entityLocalizationService.getStreakMilestoneDescription(milestone, ruLocale);

        // Then
        assertEquals("Unknown milestone description", result);
    }

    @Test
    void exerciseTypeTitleLocalization_AllTypes() {
        // Given
        ExerciseType pushUp = ExerciseType.builder().code("push_up").title("Отжимания").build();
        ExerciseType pullUp = ExerciseType.builder().code("pull_up").title("Подтягивания").build();
        ExerciseType squat = ExerciseType.builder().code("squat").title("Приседания").build();

        // Mock all exercise types for Ukrainian locale
        when(messageSource.getMessage(eq("exercise.type.push_up"), isNull(), anyString(), eq(ukLocale)))
                .thenReturn("Віджимання");
        when(messageSource.getMessage(eq("exercise.type.pull_up"), isNull(), anyString(), eq(ukLocale)))
                .thenReturn("Підтягування");
        when(messageSource.getMessage(eq("exercise.type.squat"), isNull(), anyString(), eq(ukLocale)))
                .thenReturn("Присідання");

        // When & Then
        assertEquals("Віджимання", entityLocalizationService.getExerciseTypeTitle(pushUp, ukLocale));
        assertEquals("Підтягування", entityLocalizationService.getExerciseTypeTitle(pullUp, ukLocale));
        assertEquals("Присідання", entityLocalizationService.getExerciseTypeTitle(squat, ukLocale));
    }

    @Test
    void rankTitleLocalization_SampleRanks() {
        // Given
        Rank amoeba = Rank.builder().code("amoeba").title("Амеба").build();
        Rank god = Rank.builder().code("god").title("Бог").build();

        when(messageSource.getMessage(eq("rank.amoeba"), isNull(), eq("Амеба"), eq(enLocale)))
                .thenReturn("Amoeba");
        when(messageSource.getMessage(eq("rank.god"), isNull(), eq("Бог"), eq(enLocale)))
                .thenReturn("God");

        // When & Then
        assertEquals("Amoeba", entityLocalizationService.getRankTitle(amoeba, enLocale));
        assertEquals("God", entityLocalizationService.getRankTitle(god, enLocale));
    }
}
