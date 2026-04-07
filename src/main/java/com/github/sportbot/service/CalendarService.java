package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseSummary;
import com.github.sportbot.repository.ExerciseDailyProjection;
import com.github.sportbot.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final ExerciseRecordRepository exerciseRecordRepository;

    public Map<String, List<ExerciseSummary>> getMonthData(Long telegramId, Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<ExerciseDailyProjection> dailyRecords = exerciseRecordRepository.getDailyUserProgress(
                telegramId, startDate, endDate
        );

        Map<String, List<ExerciseSummary>> monthData = new HashMap<>();

        for (ExerciseDailyProjection record : dailyRecords) {
            String dateStr = record.getDate().toString();

            monthData.computeIfAbsent(dateStr, k -> new ArrayList<>())
                    .add(new ExerciseSummary(record.getDate(), record.getExerciseType(), record.getTotalCount()));
        }

        return monthData;
    }
}
