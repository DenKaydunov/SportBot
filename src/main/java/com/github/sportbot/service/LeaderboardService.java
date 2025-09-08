package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Period;
import com.github.sportbot.repository.LeaderBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderBoardRepository leaderBoardRepository;
    private final ExerciseService exerciseService;

    public String getLeaderboardByPeriod(String exerciseCode, int limit, String periodCode) {
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);
        Period period = Period.fromCode(periodCode);
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = (startDate == null ? null : LocalDate.now());

        return buildAndFormatLeaderboard(exerciseType, limit, startDate, endDate, period.getDisplayName());
    }

    public String getLeaderboardByDates(String exerciseCode, int limit,
                                        LocalDate startDate, LocalDate endDate) {
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);
        String displayName = String.format("c %s по %s", startDate, endDate);

        return buildAndFormatLeaderboard(exerciseType, limit, startDate, endDate, displayName);
    }

    private String buildAndFormatLeaderboard(ExerciseType exerciseType, int limit,
                                             LocalDate startDate, LocalDate endDate,
                                             String periodDisplay) {
        int totalCount = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), startDate, endDate);
        List<LeaderboardEntry> entries = leaderBoardRepository.findTopUsersByExerciseTypeAndDate(
                        exerciseType.getId(), limit, startDate, endDate).stream()
                .map(this::mapRowToEntry)
                .toList();
        return formatLeaderboardString(totalCount, entries, exerciseType, periodDisplay);
    }

    private LeaderboardEntry mapRowToEntry(Object[] row) {
        String name = (String) row[0];
        Long total = (Long) row[1];
        Integer max = (Integer) row[2];
        return new LeaderboardEntry(name, total, max);
    }

    private String formatLeaderboardString(int totalCount,
                                           List<LeaderboardEntry> entries,
                                           ExerciseType exerciseType,
                                           String periodDisplay) {
        StringBuilder sb = new StringBuilder();
        sb.append("⚡Таблица лидеров⚡").append("\n");

        if (periodDisplay != null && !periodDisplay.isBlank()) {
            sb.append("Период: ").append(periodDisplay).append("\n");
        }

        sb.append("Всего пользователи сделали: ")
                .append(totalCount)
                .append(" ")
                .append(exerciseType.getTitle().toLowerCase())
                .append(".\n");

        int index = 1;
        for (LeaderboardEntry e : entries) {
            sb.append(String.format("%d. %s — %d • max %d%n",
                    index++, e.name(), e.total(), e.max()));
        }

        if (entries.isEmpty()) {
            sb.append("Нет записей за выбранный период.");
        }

        return sb.toString();
    }

    private record LeaderboardEntry(String name, long total, int max) {
    }
}
