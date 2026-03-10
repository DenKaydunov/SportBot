package com.github.sportbot.service;

import com.github.sportbot.config.WorkoutProperties;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.LeaderBoardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderBoardRepository leaderBoardRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final TagService tagService;
    private final WorkoutProperties workoutProperties;
    private final UserService userService;

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
//??
    public String getTopAllExercises(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 100)); // защита от мусора
        List<Object[]> rows = leaderBoardRepository.findTopAllWithUser(safeLimit, userId);

        // top = все строки position <= limit
        List<Object[]> topRows = rows.stream()
                .filter(r -> ((Number) r[3]).intValue() <= safeLimit)
                .toList();

        // userRow = строка userId (может уже быть в top)
        Object[] userRow = rows.stream()
                .filter(r -> ((Number) r[0]).longValue() == userId)
                .findFirst()
                .orElse(null);

        StringBuilder sb = new StringBuilder();
        sb.append("Топ:\n");

        for (Object[] r : topRows) {
            long id = ((Number) r[0]).longValue();
            String name = (String) r[1];
            long total = ((Number) r[2]).longValue();
            int pos = ((Number) r[3]).intValue();

            sb.append(formatPlace(pos))
                    .append(" ")
                    .append(name)
                    .append(" + ")
                    .append(total)
                    .append(" упр\n");
        }

        if (userRow != null) {
            int userPos = ((Number) userRow[3]).intValue();
            long userTotal = ((Number) userRow[2]).longValue();

            sb.append("\nТвое место — ")
                    .append(userPos)
                    .append(" (ты + ")
                    .append(userTotal)
                    .append(" упр)");
        }

        return sb.toString();
    }

    private String formatPlace(int pos) {
        return switch (pos) {
            case 1 -> "🥇 1 место —";
            case 2 -> "🥈 2 место —";
            case 3 -> "🥉 3 место —";
            default -> pos + " место —";
        };
    }
    //??
    public String getTopUsersRating(Long telegramId){
        List<UserExerciseTotal> totals = leaderBoardRepository.getTotalUsersRating();
        Map<User, Double> userTotals = sumUserScore(totals);
        List<UserScore> scoreList = getTopUser(userTotals);
        User user = userService.getUserByTelegramId(telegramId);
        int userPosition = userPosition(scoreList, user);
        String allRating = messageBuildRating(scoreList, userPosition);
        String userRating = messageBuildUserRating(scoreList, userPosition);
        return allRating + userRating;
    }

    private Map<User, Double> sumUserScore(List<UserExerciseTotal> totals){
        return totals.stream()
                .collect(Collectors.groupingBy(
                        UserExerciseTotal::user,
                        Collectors.summingDouble(uet ->
                                uet.total() * workoutProperties.getCoefficient(uet.exerciseType().getCode())
                        )));
    }

    private List<UserScore> getTopUser(Map<User, Double> totals){
        return totals.entrySet().stream()
                .filter(u -> u.getKey().getIsSubscribed())
                .sorted(Map.Entry.<User, Double> comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new UserScore(entry.getKey(), entry.getValue()))
                .toList();
    }

    private int userPosition(List<UserScore> scoreList, User user){
        return IntStream.range(0, scoreList.size())
                .filter(i -> scoreList.get(i).user().equals(user))
                .findFirst()
                .orElse(-1);
    }

    /**
     *
     * @param scoreList
     * @param userPosition
     * @return Example:
     * 🏆Top 10:
     * 🥇 1 место Иван - 110.00
     * 🥈 2 место Андрей - 98.00
     * ➡️🥉 3 место Сергей - 86.00
     * ⭐ 4 место Анна - 75.38
     * ⭐ 5 место Николай - 73.83
     */
    private String messageBuildRating(List<UserScore> scoreList, int userPosition){
        StringBuilder message = new StringBuilder("🏆Top 10:");
        String[] medals = {"🥇", "🥈", "🥉"};
        int count = 1;

        for (UserScore us : scoreList){
            String medal = (count <= 3) ? medals[count - 1] : "⭐";
            if (count == (userPosition + 1)){
                message.append("➡️");
            }
            message.append(String.format("%n%s %d место %s - %.2f",
                    medal, count, us.user().getFullName(), us.totalScore()));
            count++;
            if (count > 5){
                break;
            }
        }
        return message.toString();
    }

    /**
     *
     * @param scoreList
     * @param userPosition
     * @return Example:
     * ...
     * ⭐ 7 место Анна - 75.38
     * ➡️⭐ 8 место Николай - 73.83
     * ⭐ 9 место Николай - 73.83
     * ...
     */
    private String messageBuildUserRating(List<UserScore> scoreList, int userPosition){
        StringBuilder message = new StringBuilder();

        int start = Math.max(0, userPosition - 1);
        int end = Math.min(scoreList.size() - 1, userPosition + 1);

        if (userPosition >= 5){
            message.append("\n...");
        for (int i = start; i <= end; i++) {
            if (i == userPosition) {
                message.append("➡️");
            }
            message.append(String.format("%n⭐ %d место %s - %.2f",
                    i + 1,
                    scoreList.get(i).user().getFullName(),
                    scoreList.get(i).totalScore()));
        }
            message.append("\n...");
        }
        return message.toString();
    }

    private record LeaderboardEntry(String name, long total) {
    }
}
