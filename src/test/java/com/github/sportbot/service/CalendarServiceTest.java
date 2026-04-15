package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseSummary;
import com.github.sportbot.repository.ExerciseDailyProjection;
import com.github.sportbot.repository.ExerciseRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalendarServiceTest {

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void getMonthData_whenNoRecords_shouldReturnEmptyMap() {
        // Given
        Long telegramId = 123456L;
        when(exerciseRecordRepository.getDailyUserProgress(eq(telegramId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // When
        Map<String, List<ExerciseSummary>> result = calendarService.getMonthData(telegramId, 2026, 4);

        // Then
        assertThat(result).isEmpty();
        verify(exerciseRecordRepository).getDailyUserProgress(
                eq(telegramId),
                eq(LocalDate.of(2026, 4, 1)),
                eq(LocalDate.of(2026, 4, 30))
        );
    }

    @Test
    void getMonthData_whenSingleDayRecords_shouldGroupByDate() {
        // Given
        Long telegramId = 123456L;
        LocalDate date = LocalDate.of(2026, 4, 15);

        ExerciseDailyProjection projection1 = createProjection(date, "pushups", 50);
        ExerciseDailyProjection projection2 = createProjection(date, "squats", 100);

        when(exerciseRecordRepository.getDailyUserProgress(eq(telegramId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(projection1, projection2));

        // When
        Map<String, List<ExerciseSummary>> result = calendarService.getMonthData(telegramId, 2026, 4);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get("2026-04-15")).hasSize(2);
        assertThat(result.get("2026-04-15"))
                .extracting(ExerciseSummary::exerciseType)
                .containsExactlyInAnyOrder("pushups", "squats");
    }

    @Test
    void getMonthData_whenMultipleDays_shouldGroupCorrectly() {
        // Given
        Long telegramId = 123456L;
        LocalDate day1 = LocalDate.of(2026, 4, 10);
        LocalDate day2 = LocalDate.of(2026, 4, 15);

        ExerciseDailyProjection proj1 = createProjection(day1, "pushups", 50);
        ExerciseDailyProjection proj2 = createProjection(day2, "pushups", 60);
        ExerciseDailyProjection proj3 = createProjection(day2, "squats", 100);

        when(exerciseRecordRepository.getDailyUserProgress(eq(telegramId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(proj1, proj2, proj3));

        // When
        Map<String, List<ExerciseSummary>> result = calendarService.getMonthData(telegramId, 2026, 4);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get("2026-04-10")).hasSize(1);
        assertThat(result.get("2026-04-15")).hasSize(2);
        assertThat(result.get("2026-04-10").get(0).totalCount()).isEqualTo(50);
        assertThat(result.get("2026-04-15"))
                .extracting(ExerciseSummary::totalCount)
                .containsExactlyInAnyOrder(60, 100);
    }

    @Test
    void getMonthData_withFebruaryLeapYear_shouldHandleCorrectEndDate() {
        // Given
        Long telegramId = 123456L;
        when(exerciseRecordRepository.getDailyUserProgress(eq(telegramId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // When
        calendarService.getMonthData(telegramId, 2024, 2); // Leap year

        // Then
        verify(exerciseRecordRepository).getDailyUserProgress(
                eq(telegramId),
                eq(LocalDate.of(2024, 2, 1)),
                eq(LocalDate.of(2024, 2, 29)) // Leap year has 29 days
        );
    }

    @Test
    void getMonthData_withFebruaryNonLeapYear_shouldHandleCorrectEndDate() {
        // Given
        Long telegramId = 123456L;
        when(exerciseRecordRepository.getDailyUserProgress(eq(telegramId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        // When
        calendarService.getMonthData(telegramId, 2025, 2); // Non-leap year

        // Then
        verify(exerciseRecordRepository).getDailyUserProgress(
                eq(telegramId),
                eq(LocalDate.of(2025, 2, 1)),
                eq(LocalDate.of(2025, 2, 28)) // Non-leap year has 28 days
        );
    }

    private ExerciseDailyProjection createProjection(LocalDate date, String exerciseType, Integer totalCount) {
        return new ExerciseDailyProjection() {
            @Override
            public LocalDate getDate() {
                return date;
            }

            @Override
            public String getExerciseType() {
                return exerciseType;
            }

            @Override
            public Integer getTotalCount() {
                return totalCount;
            }
        };
    }
}
