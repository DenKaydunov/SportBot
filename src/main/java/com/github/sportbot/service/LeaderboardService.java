package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Period;
import com.github.sportbot.repository.WorkoutHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final WorkoutHistoryRepository workoutHistoryRepository;
    private final ExerciseService exerciseService;

    public String getLeaderboardString(String exerciseCode, int limit) {
        return getLeaderboardString(exerciseCode, limit, null);
    }

    public String getLeaderboardString(String exerciseCode, int limit, String period) {
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);
        LocalDate startDate = getStartDateForPeriod(period);
        LeaderboardData leaderboardData = buildLeaderboardData(exerciseType, limit, startDate);
        
        return formatLeaderboardString(leaderboardData, exerciseType, period);
    }

    private LocalDate getStartDateForPeriod(String period) {
        return Period.fromCode(period)
                .map(Period::getStartDate)
                .orElse(null);
    }

    private String getPeriodDisplayName(String period) {
        return Period.fromCode(period)
                .map(Period::getDisplayName)
                .orElse(Period.ALL.getDisplayName());
    }

    private LeaderboardData buildLeaderboardData(ExerciseType exerciseType, int limit, LocalDate startDate) {
        int totalCount;
        List<Object[]> rawData;
        
        if (startDate != null) {
            totalCount = workoutHistoryRepository.sumAllByExerciseTypeAndDateAfter(exerciseType.getId(), startDate);
            rawData = workoutHistoryRepository.findTopUsersWithDetailsByExerciseTypeAndDateAfter(exerciseType.getId(), limit, startDate);
        } else {
            totalCount = workoutHistoryRepository.sumAllByExerciseType(exerciseType.getId());
            rawData = workoutHistoryRepository.findTopUsersWithDetailsByExerciseType(exerciseType.getId(), limit);
        }
        
        List<LeaderboardEntry> entries = rawData.stream()
                .map(this::buildLeaderboardEntryFromDetailedRow)
                .toList();
        
        return new LeaderboardData(totalCount, entries);
    }

    private LeaderboardEntry buildLeaderboardEntryFromDetailedRow(Object[] row) {
        Long total = (Long) row[1];
        String name = (String) row[2];
        Integer max = (Integer) row[3];
        
        return new LeaderboardEntry(name, total, max);
    }



    //кодище №2


    private String formatLeaderboardString(LeaderboardData data, ExerciseType exerciseType, String period) {
        StringBuilder sb = new StringBuilder();
        
        appendHeader(sb, data.totalCount(), exerciseType, period);
        appendEntries(sb, data.entries());
        
        return sb.toString();
    }

    private void appendHeader(StringBuilder sb, int totalCount, ExerciseType exerciseType, String period) {
        sb.append("⚡Таблица лидеров⚡\n");
        
        String periodText = getPeriodDisplayName(period);
        if (periodText != null) {
            sb.append("Период: ").append(periodText).append("\n");
        }
        
        sb.append("Всего пользователи сделали: ")
          .append(totalCount)
          .append(" ")
          .append(exerciseType.getTitle().toLowerCase())
          .append(".\n");
    }

    private void appendEntries(StringBuilder sb, List<LeaderboardEntry> entries) {
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry entry = entries.get(i);
            sb.append(String.format("%d. %s - %d • max %d%n", 
                i + 1, entry.name(), entry.total(), entry.max()));
        }
    }

    private record LeaderboardData(int totalCount, List<LeaderboardEntry> entries) {
    }

    private record LeaderboardEntry(String name, Long total, Integer max) {
    }
}
