package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorProjection;
import com.github.sportbot.repository.CompetitorsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitorsServiceTest {

    @Mock
    private CompetitorsRepository competitorsRepository;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @InjectMocks
    private CompetitorsService competitorsService;

    private User testUser;
    private ExerciseType exerciseType;
    private Locale locale;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);
        testUser.setTelegramId(123456L);
        testUser.setLanguage("ru");

        exerciseType = new ExerciseType();
        exerciseType.setId(1L);
        exerciseType.setCode("pushups");

        locale = new Locale("ru");
    }

    @Test
    void getCompetitorsAllTime_whenNoCompetitors_shouldReturnNoDataMessage() {
        // Given
        when(userService.getUserByTelegramId(123456L)).thenReturn(testUser);
        when(userService.getUserLocale(testUser)).thenReturn(locale);
        when(exerciseTypeService.getExerciseType("pushups")).thenReturn(exerciseType);
        when(competitorsRepository.findCompetitors(1, 1L)).thenReturn(List.of());

        when(messageSource.getMessage(eq("competitors.header"), any(), eq(locale))).thenReturn("Конкуренты");
        when(messageSource.getMessage(eq("competitors.exercise.label"), any(), eq(locale))).thenReturn("Упражнение: Отжимания");
        when(messageSource.getMessage(eq("competitors.period.all.time"), any(), eq(locale))).thenReturn("За всё время");
        when(messageSource.getMessage(eq("competitors.no.data"), any(), eq(locale))).thenReturn("Нет данных");
        when(entityLocalizationService.getExerciseTypeTitle(exerciseType, locale)).thenReturn("Отжимания");

        // When
        String result = competitorsService.getCompetitorsAllTime("pushups", 123456L);

        // Then
        assertThat(result).contains("Конкуренты");
        assertThat(result).contains("Нет данных");
        verify(competitorsRepository).findCompetitors(1, 1L);
    }

    @Test
    void getCompetitorsAllTime_withCompetitors_shouldFormatCorrectly() {
        // Given
        CompetitorProjection comp1 = createCompetitor(1, 1, "John Doe", 150L);
        CompetitorProjection comp2 = createCompetitor(2, 2, "Jane Smith", 200L);

        when(userService.getUserByTelegramId(123456L)).thenReturn(testUser);
        when(userService.getUserLocale(testUser)).thenReturn(locale);
        when(exerciseTypeService.getExerciseType("pushups")).thenReturn(exerciseType);
        when(competitorsRepository.findCompetitors(1, 1L)).thenReturn(List.of(comp1, comp2));

        when(messageSource.getMessage(eq("competitors.header"), any(), eq(locale))).thenReturn("Конкуренты");
        when(messageSource.getMessage(eq("competitors.exercise.label"), any(), eq(locale))).thenReturn("Упражнение: Отжимания");
        when(messageSource.getMessage(eq("competitors.period.all.time"), any(), eq(locale))).thenReturn("За всё время");
        when(entityLocalizationService.getExerciseTypeTitle(exerciseType, locale)).thenReturn("Отжимания");

        // When
        String result = competitorsService.getCompetitorsAllTime("pushups", 123456L);

        // Then
        assertThat(result).contains("Конкуренты");
        assertThat(result).contains("👉 1. John Doe — 150");
        assertThat(result).contains("2. Jane Smith — 200");
        verify(competitorsRepository).findCompetitors(1, 1L);
    }

    @Test
    void getCompetitorsAllTime_whenCurrentUserIsFirst_shouldMarkWithPointer() {
        // Given
        CompetitorProjection currentUser = createCompetitor(1, 1, "Me", 500L);
        CompetitorProjection other = createCompetitor(3, 2, "Other", 300L);

        when(userService.getUserByTelegramId(123456L)).thenReturn(testUser);
        when(userService.getUserLocale(testUser)).thenReturn(locale);
        when(exerciseTypeService.getExerciseType("pushups")).thenReturn(exerciseType);
        when(competitorsRepository.findCompetitors(1, 1L)).thenReturn(List.of(currentUser, other));

        when(messageSource.getMessage(eq("competitors.header"), any(), eq(locale))).thenReturn("Конкуренты");
        when(messageSource.getMessage(eq("competitors.exercise.label"), any(), eq(locale))).thenReturn("Упражнение: Отжимания");
        when(messageSource.getMessage(eq("competitors.period.all.time"), any(), eq(locale))).thenReturn("За всё время");
        when(entityLocalizationService.getExerciseTypeTitle(exerciseType, locale)).thenReturn("Отжимания");

        // When
        String result = competitorsService.getCompetitorsAllTime("pushups", 123456L);

        // Then
        assertThat(result).contains("👉 1. Me — 500");
        assertThat(result).contains("2. Other — 300");
        assertThat(result).doesNotContain("👉 2.");
    }

    private CompetitorProjection createCompetitor(Integer userId, Integer position, String fullName, Long total) {
        return new CompetitorProjection() {
            @Override
            public Integer getUserId() {
                return userId;
            }

            @Override
            public Integer getPosition() {
                return position;
            }

            @Override
            public String getFullName() {
                return fullName;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }
}
