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
        Long tag = tagService.getIdByCode(tagCode);
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

    public String getRating(Long telegramId) {
        List<UserExerciseSummary> totals = leaderBoardRepository.getTotalUsersRating();
        Map<User, Double> userTotals = sumUserScore(totals);
        List<UserScore> scoreList = getTopUser(userTotals);
        User user = userService.getUserByTelegramId(telegramId);
        int userPosition = userPosition(scoreList, user);
        String allRating = messageBuildRating(scoreList, userPosition);
        String userRating = messageBuildUserRating(scoreList, userPosition);
        return allRating + userRating;
    }

    private Map<User, Double> sumUserScore(List<UserExerciseSummary> totals) {
        return totals.stream()
                .collect(Collectors.groupingBy(
                        UserExerciseSummary::user,
                        Collectors.summingDouble(uet ->
                                uet.total() * workoutProperties.getCoefficient(uet.exerciseType().getCode())
                        )));
    }

    private List<UserScore> getTopUser(Map<User, Double> totals) {
        return totals.entrySet().stream()
                .filter(u -> u.getKey().getIsSubscribed())
                .sorted(Map.Entry.<User, Double>comparingByValue(Comparator.reverseOrder()))
                .map(entry -> new UserScore(entry.getKey(), entry.getValue()))
                .toList();
    }

    private int userPosition(List<UserScore> scoreList, User user) {
        return IntStream.range(0, scoreList.size())
                .filter(i -> scoreList.get(i).user().equals(user))
                .findFirst()
                .orElse(-1);
    }

    /**
     * Builds a formatted message for the top 5 leaderboard, including user positions, medals, and scores.
     *
     * @param scoreList List of {@link UserScore} representing all users’ scores, sorted in descending order.
     * @param userPosition Position of the target user in the leaderboard (zero-based index), or -1 if not found.
     * @return A formatted string displaying the top 5 leaderboard. If the target user is in the top 5,
     *         they are marked with ⬅️. Example output:
     *         🏆Top 5:
     *         🥇 1 место Иван - 110.00
     *         🥈 2 место Андрей - 98.00
     *         🥉 3 место Сергей - 86.00 ⬅️
     *         ⭐ 4 место Анна - 75.38
     *         ⭐ 5 место Николай - 73.83
     */
    private String messageBuildRating(List<UserScore> scoreList, int userPosition) {
        StringBuilder message = new StringBuilder("🏆Top 5:");
        String[] medals = {"🥇", "🥈", "🥉"};
        int count = 1;

        for (UserScore us : scoreList) {
            String medal = (count <= 3) ? medals[count - 1] : "⭐";

            if (userPosition >= 0 && count == (userPosition + 1)) {
                message.append(String.format("%n%s %d место %s - %.2f ⬅️",
                        medal, count, us.user().getFullName(), us.totalScore()));
            } else {
                message.append(String.format("%n%s %d место %s - %.2f",
                        medal, count, us.user().getFullName(), us.totalScore()));
            }

            count++;
            if (count > 5) {
                break;
            }
        }
        return message.toString();
    }

    /**
     * Строит строку с контекстом позиции пользователя в рейтинге.
     *
     * @param scoreList полный список рейтинга
     * @param userPosition позиция пользователя (0-based), или -1 если не найден
     * @return Форматированная строка с позицией и контекстом
     *
     * Примеры вывода:
     * - Пользователь в топ-5: возвращает пустую строку (позиция уже показана в топе)
     * - Пользователь вне топ-5:
     *   ...
     *   ⭐ 7 место Анна - 75.38
     *   ⭐ 8 место Николай - 73.83 ⬅️
     *   ⭐ 9 место Петр - 70.15
     *   ...
     * - Пользователь не найден (userPosition == -1): возвращает пустую строку
     */
    private String messageBuildUserRating(List<UserScore> scoreList, int userPosition) {
        // Случай 1: Пользователь не в рейтинге (не подписан или нет упражнений)
        if (userPosition == -1) {
            return "";
        }

        // Случай 2: Пользователь в топ-5 - позиция уже показана с ⬅️ в messageBuildRating
        if (userPosition < 5) {
            return "";  // Не дублируем информацию
        }

        // Случай 3: Пользователь вне топ-5 - показываем контекст (± 1 позицию)
        StringBuilder message = new StringBuilder();
        int start = Math.max(0, userPosition - 1);
        int end = Math.min(scoreList.size() - 1, userPosition + 1);

        message.append("\n...");
        for (int i = start; i <= end; i++) {
            if (i == userPosition) {
                message.append(String.format("%n⭐ %d место %s - %.2f ⬅️",
                        i + 1,
                        scoreList.get(i).user().getFullName(),
                        scoreList.get(i).totalScore()));
            } else {
                message.append(String.format("%n⭐ %d место %s - %.2f",
                        i + 1,
                        scoreList.get(i).user().getFullName(),
                        scoreList.get(i).totalScore()));
            }
        }
        message.append("\n...");

        return message.toString();
    }

    private record LeaderboardEntry(String name, long total) {
    }
}
