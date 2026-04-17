package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final UserRepository userRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MessageLocalizer messageLocalizer;
    private final ExerciseTypeService exerciseTypeService;
    private final RankService rankService;
    private final StreakService streakService;
    private final AchievementService achievementService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final EntityLocalizationService entityLocalizationService;
    private final UnifiedAchievementService unifiedAchievementService;
    private final AchievementDefinitionRepository achievementDefinitionRepository;


    @Transactional
    public String saveExerciseResult(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(req);
        Locale locale = userService.getUserLocale(user);

        ExerciseRecord exercise = ExerciseRecord.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(req.count())
                .date(LocalDate.now())
                .build();

        user.getExerciseRecords().add(exercise);

        notificationService.notifyFollowersAboutWorkout(user, exerciseType, req.count());

        // Обновляем стрик пользователя
        streakService.updateStreak(user, exercise.getDate());

        String achievement = getAchievementUpdateMessage(user);

        // Check achievements using unified service
        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();
        unifiedAchievementService.checkAchievements(trigger);

        // Перезагружаем пользователя для получения обновленных данных стрика
        user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        int total = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);

        String message = messageLocalizer.localize(
                "workout.reps_recorded",
                new Object[]{entityLocalizationService.getExerciseTypeTitle(exerciseType, locale), req.count(), total},
                locale);
        String rankMessage = rankService.assignRankIfEligible(user);

        // Добавляем информацию о стрике, если он изменился
        String streakMessage = getStreakUpdateMessage(user, exercise.getDate());


        String nextAchievement = getNextAchievementUpdateMessage(user);

        return message + rankMessage + streakMessage + achievement + nextAchievement;
    }

    public int getTotalReps(User user, String exerciseCode) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        return exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
    }

    /**
     * Получает сообщение об обновлении стрика, если стрик изменился.
     */
    private String getStreakUpdateMessage(User user, LocalDate workoutDate) {
        LocalDate lastWorkoutDate = user.getLastWorkoutDate();
        Locale locale = userService.getUserLocale(user);

        // Если это первая тренировка или стрик увеличился
        if (lastWorkoutDate == null || (workoutDate.equals(LocalDate.now()) && lastWorkoutDate.equals(LocalDate.now()
                .minusDays(1)))) {

            int currentStreak = user.getCurrentStreak();
            if (currentStreak > 1) {
                return messageLocalizer.localize(
                        "workout.streak_updated",
                        new Object[] { currentStreak },
                        locale);
            }
        }

        return "";
    }

    private String getAchievementUpdateMessage(User user){
        int currentStreak = user.getCurrentStreak();
        Locale locale = userService.getUserLocale(user);

        // Get all streak achievement definitions, sorted by target value
        List<AchievementDefinition> streakDefinitions = achievementDefinitionRepository
                .findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK);

        // Get user's completed achievements
        List<UserAchievement> completedAchievements = unifiedAchievementService
                .getCompletedAchievements(user.getId());

        List<Long> completedDefinitionIds = completedAchievements.stream()
                .map(ua -> ua.getAchievementDefinition().getId())
                .toList();

        StringBuilder message = new StringBuilder();

        for(AchievementDefinition def : streakDefinitions) {
            if (!completedDefinitionIds.contains(def.getId()) && currentStreak >= def.getTargetValue()) {
                message.append("\n").append(messageLocalizer.localize(
                    "exercise.achievement.congrats",
                    new Object[]{
                        def.getTargetValue(),
                        entityLocalizationService.getAchievementTitle(def, locale),
                        entityLocalizationService.getAchievementDescription(def, locale),
                        def.getRewardTon()
                    },
                    locale
                ));
            }
        }
        return message.toString();
    }

    private String getNextAchievementUpdateMessage(User user) {
        int currentStreak = user.getCurrentStreak();
        Locale locale = userService.getUserLocale(user);

        // Get all streak achievement definitions, sorted by target value
        List<AchievementDefinition> streakDefinitions = achievementDefinitionRepository
                .findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK);

        // Get user's completed achievements
        List<UserAchievement> completedAchievements = unifiedAchievementService
                .getCompletedAchievements(user.getId());

        List<Long> completedDefinitionIds = completedAchievements.stream()
                .map(ua -> ua.getAchievementDefinition().getId())
                .toList();

        StringBuilder message = new StringBuilder(
            messageLocalizer.localize("exercise.all.achievements.earned", null, locale)
        );

        for (AchievementDefinition def : streakDefinitions) {
            if (!completedDefinitionIds.contains(def.getId()) && def.getTargetValue() > currentStreak) {
                int daysToNext = def.getTargetValue() - currentStreak;
                message.setLength(0);
                message.append("\n").append(messageLocalizer.localize(
                    "exercise.next.achievement.hint",
                    new Object[]{daysToNext},
                    locale
                ));
                break;
            }
        }
        return message.toString();
    }

    /** Provides user exercises for a specified date
     * <p>
     * Твой прогресс за 25.02.2026:
     * Приседания - 0
     * Подтягивания - 20
     * Отжимания - 10
     * Пресс - 0
     *
     */
    public List<ExercisePeriodProjection> getUserProgress(
            Long telegramId,
            LocalDate startDate,
            LocalDate endDate) {
        return exerciseRecordRepository.getUserProgressByPeriod(telegramId, startDate, endDate);
    }

    public String progressForPeriod(
            Long telegramId,
            LocalDate startDate,
            LocalDate endDate) {
        User user = userRepository.findByTelegramId(telegramId).orElseThrow(UserNotFoundException::new);
        Locale locale = userService.getUserLocale(user);

        LocalDate finalEndDate = (endDate == null) ? startDate : endDate;
        verifyDates(startDate, finalEndDate, locale);

        List<ExercisePeriodProjection> summary = exerciseRecordRepository.getUserProgressByPeriod(telegramId, startDate, finalEndDate);

        StringBuilder report = new StringBuilder();
        appendHeader(report, startDate, finalEndDate, locale);

        int totalCount = 0;

        for (ExercisePeriodProjection list : summary){
            totalCount += list.getTotalCount();
        }

        if (totalCount > 0)  {
            summary.forEach(exercise -> {
                String localizedExerciseType = messageLocalizer.localize(
                    "exercise.type." + exercise.getExerciseType(),
                    null,
                    locale
                );
                report.append(String.format("%s - %d%n", localizedExerciseType, exercise.getTotalCount()));
            });
        } else {
            report.append(messageLocalizer.localize(
                "exercise.no.workouts.found", null, locale
            ));
        }
        return report.toString();
    }

    private void appendHeader(StringBuilder sb, LocalDate start, LocalDate end, Locale locale) {
        if (start.equals(end)) {
            sb.append(messageLocalizer.localize(
                "exercise.progress.for.date",
                new Object[]{start.format(ExerciseService.DATE_FORMATTER)},
                locale
            )).append("\n");
        } else {
            sb.append(messageLocalizer.localize(
                "exercise.progress.period",
                new Object[]{start.format(ExerciseService.DATE_FORMATTER), end.format(ExerciseService.DATE_FORMATTER)},
                locale
            )).append("\n");
        }
    }

    private void verifyDates(LocalDate startDate, LocalDate endDate, Locale locale) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                messageLocalizer.localize("exercise.error.invalid.dates", null, locale)
            );
        }
    }
}
