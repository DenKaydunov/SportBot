package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.LeaderBoardRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyLeaderboardService {

    private static final int TOP_LIMIT = 3;
    private static final String[] MEDALS = {"🥇", "🥈", "🥉"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Lazy
    private final SportBot sportBot;
    private final LeaderBoardRepository leaderBoardRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;
    private final UserService userService;

    public void sendWeeklyCongratulations() {
        log.info("Starting weekly leaderboard congratulations");

        WeekDateRange weekRange = calculatePreviousWeekRange();
        LocalDate startDate = weekRange.startDate();
        LocalDate endDate = weekRange.endDate();

        log.info("Processing week: {} - {}", startDate, endDate);

        List<User> subscribedUsers = userRepository.findAllByIsSubscribedTrue();
        log.info("Found {} subscribed users", subscribedUsers.size());

        if (subscribedUsers.isEmpty()) {
            log.info("No subscribed users to notify");
            return;
        }

        // Fetch exercise types and leaderboard data once for all users
        List<ExerciseType> exerciseTypes = exerciseTypeRepository.findAll();
        WeeklyLeaderboardData leaderboardData = fetchLeaderboardData(exerciseTypes, startDate, endDate);

        for (User user : subscribedUsers) {
            try {
                String message = buildWeeklyLeaderboardMessage(startDate, endDate, user, exerciseTypes, leaderboardData);
                sportBot.sendTgMessage(user.getTelegramId(), message);
            } catch (Exception e) {
                log.error("Failed to send weekly leaderboard to user {}", user.getTelegramId(), e);
            }
        }

        log.info("Weekly leaderboard sending completed");
    }

    private WeekDateRange calculatePreviousWeekRange() {
        LocalDate now = LocalDate.now();
        // Находим прошлое воскресенье (конец прошлой недели)
        LocalDate endDate = now.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        // Начало недели = 6 дней назад от воскресенья
        LocalDate startDate = endDate.minusDays(6);
        return new WeekDateRange(startDate, endDate);
    }

    private WeeklyLeaderboardData fetchLeaderboardData(List<ExerciseType> exerciseTypes, LocalDate startDate, LocalDate endDate) {
        WeeklyLeaderboardData data = new WeeklyLeaderboardData();

        for (ExerciseType exerciseType : exerciseTypes) {
            List<Object[]> topUsers = leaderBoardRepository.findTopUsersByExerciseTypeAndDate(
                exerciseType.getId(),
                null,
                TOP_LIMIT,
                startDate,
                endDate
            );
            data.putTopUsers(exerciseType.getId(), topUsers);
        }

        return data;
    }

    private String buildWeeklyLeaderboardMessage(LocalDate startDate, LocalDate endDate, User user,
                                                  List<ExerciseType> exerciseTypes, WeeklyLeaderboardData leaderboardData) {
        Locale locale = userService.getUserLocale(user);
        StringBuilder message = new StringBuilder();

        String header = messageSource.getMessage(
            "weekly.leaderboard.header",
            new Object[]{startDate.format(DATE_FORMATTER), endDate.format(DATE_FORMATTER)},
            locale
        );
        message.append(header);

        boolean hasAnyRecords = false;

        for (ExerciseType exerciseType : exerciseTypes) {
            List<Object[]> topUsers = leaderboardData.getTopUsers(exerciseType.getId());

            if (!topUsers.isEmpty()) {
                hasAnyRecords = true;
                String exerciseTitle = entityLocalizationService.getExerciseTypeTitle(exerciseType, locale);
                String exerciseHeader = messageSource.getMessage(
                    "weekly.leaderboard.exercise.header",
                    new Object[]{exerciseTitle},
                    locale
                );
                message.append(exerciseHeader);

                for (int i = 0; i < topUsers.size(); i++) {
                    Object[] row = topUsers.get(i);
                    String userName = (String) row[0];
                    Long total = (Long) row[1];
                    String medal = MEDALS[i];

                    String placeText = messageSource.getMessage(
                        "weekly.leaderboard.place",
                        new Object[]{medal + " " + (i + 1), userName, total},
                        locale
                    );
                    message.append("\n").append(placeText);
                }
            }
        }

        if (!hasAnyRecords) {
            String noRecords = messageSource.getMessage("weekly.leaderboard.no.records", null, locale);
            message.append("\n\n").append(noRecords);
        }

        return message.toString();
    }

    private record WeekDateRange(LocalDate startDate, LocalDate endDate) {
    }

    private static class WeeklyLeaderboardData {
        private final java.util.Map<Long, List<Object[]>> topUsersByExerciseType = new java.util.HashMap<>();

        public void putTopUsers(Long exerciseTypeId, List<Object[]> topUsers) {
            topUsersByExerciseType.put(exerciseTypeId, topUsers);
        }

        public List<Object[]> getTopUsers(Long exerciseTypeId) {
            return topUsersByExerciseType.getOrDefault(exerciseTypeId, java.util.Collections.emptyList());
        }
    }
}
