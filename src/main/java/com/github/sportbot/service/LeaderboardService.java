package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Period;
import com.github.sportbot.repository.LeaderBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderBoardRepository leaderBoardRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final TagService tagService;

    public String getLeaderboardByPeriod(String exerciseCode, int limit, String periodCode) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        Period period = Period.fromCode(periodCode);
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = (startDate == null ? null : LocalDate.now());

        return buildAndFormatLeaderboard(exerciseType, null, limit, startDate, endDate, period.getDisplayName());
    }

    public String getLeaderboardByPeriodPaged(String exerciseCode, Pageable pageable, String periodCode) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        Period period = Period.fromCode(periodCode);
        LocalDate startDate = period.getStartDate();
        LocalDate endDate = (startDate == null ? null : LocalDate.now());

        Pageable unpagedSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        int totalCount = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), null, startDate, endDate);
        List<LeaderboardEntry> entries = leaderBoardRepository.findTopUsersByExerciseTypeAndDatePaged(
                        exerciseType.getId(), null, startDate, endDate, unpagedSort).stream()
                .map(this::mapRowToEntry)
                .toList();

        String periodDisplay = period.getDisplayName();
        return formatLeaderboardString(totalCount, entries, exerciseType, periodDisplay, (int) pageable.getOffset());
    }

    public String getLeaderboardByDates(String exerciseCode,
                                        String tagCode,
                                        int limit,
                                        LocalDate startDate,
                                        LocalDate endDate) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        Long tag= tagService.getIdByCode(tagCode);
        String displayName = String.format("c %s по %s", startDate, endDate);

        return buildAndFormatLeaderboard(exerciseType, tag, limit, startDate, endDate, displayName);
    }

    public String getLeaderboardByDatesPaged(String exerciseCode,
                                             String tagCode,
                                             Pageable pageable,
                                             LocalDate startDate,
                                             LocalDate endDate) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        Long tagId = tagService.getIdByCode(tagCode);
        String displayName = String.format("c %s по %s", startDate, endDate);

        Pageable unpagedSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        int totalCount = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), tagId, startDate, endDate);
        List<LeaderboardEntry> entries = leaderBoardRepository.findTopUsersByExerciseTypeAndDatePaged(
                        exerciseType.getId(), tagId, startDate, endDate, unpagedSort).stream()
                .map(this::mapRowToEntry)
                .toList();

        return formatLeaderboardString(totalCount, entries, exerciseType, displayName, (int) pageable.getOffset());
    }

    private String buildAndFormatLeaderboard(ExerciseType exerciseType,
                                             Long tagId,
                                             int limit,
                                             LocalDate startDate,
                                             LocalDate endDate,
                                             String periodDisplay) {
        int totalCount = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), tagId, startDate, endDate);
        List<LeaderboardEntry> entries = leaderBoardRepository.findTopUsersByExerciseTypeAndDate(
                        exerciseType.getId(), tagId, limit, startDate, endDate).stream()
                .map(this::mapRowToEntry)
                .toList();
        return formatLeaderboardString(totalCount, entries, exerciseType, periodDisplay, 0);
    }

    private LeaderboardEntry mapRowToEntry(Object[] row) {
        String name = (String) row[0];
        Long total = (Long) row[1];
        return new LeaderboardEntry(name, total);
    }

    private String formatLeaderboardString(int totalCount,
                                           List<LeaderboardEntry> entries,
                                           ExerciseType exerciseType,
                                           String periodDisplay,
                                           int startIndex) {
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

        int index = startIndex + 1;
        for (LeaderboardEntry e : entries) {
            sb.append(String.format("%d. %s — %d%n",
                    index++, e.name(), e.total()));
        }

        if (entries.isEmpty()) {
            sb.append("Нет записей за выбранный период.");
        }

        return sb.toString();
    }

    private record LeaderboardEntry(String name, long total) {
    }
}
