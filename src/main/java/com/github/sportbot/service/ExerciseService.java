package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.StreakMilestone;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
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
    private final MessageSource messageSource;
    private final ExerciseTypeService exerciseTypeService;
    private final RankService rankService;
    private final StreakService streakService;
    private final MilestoneRepository milestoneRepository;
    private final AchievementRepository achievementRepository;
    private final AchievementService achievementService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final EntityLocalizationService entityLocalizationService;


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

        achievementService.checkStreakMilestones(req.telegramId());

        // Перезагружаем пользователя для получения обновленных данных стрика
        user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        int total = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);

        String message = messageSource.getMessage(
                "workout.reps_recorded",
                new Object[]{entityLocalizationService.getExerciseTypeTitle(exerciseType, locale), req.count(), total},
                locale);
        String rankMessage = rankService.assignRankIfEligible(user, exerciseType, total);

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
                return messageSource.getMessage(
                        "workout.streak_updated",
                        new Object[] { currentStreak },
                        locale);
            }
        }

        return "";
    }

    private String getAchievementUpdateMessage(User user){
        int currentStreak = user.getCurrentStreak();
        List<StreakMilestone> milestone = milestoneRepository.findAllByOrderByDaysRequiredAsc();
        List<Long> achieve = achievementRepository.findMilestoneIdsByUserId(user.getId());
        Locale locale = userService.getUserLocale(user);

        StringBuilder message = new StringBuilder();

        for(StreakMilestone m : milestone) {
            if (!achieve.contains(m.getId()) && currentStreak >= m.getDaysRequired()) {
                message.append("\n").append(messageSource.getMessage(
                    "exercise.achievement.congrats",
                    new Object[]{m.getDaysRequired(), entityLocalizationService.getStreakMilestoneTitle(m, locale), entityLocalizationService.getStreakMilestoneDescription(m, locale), m.getRewardTon()},
                    locale
                ));
            }
        }
        return message.toString();
    }

    private String getNextAchievementUpdateMessage(User user) {
        int currentStreak = user.getCurrentStreak();
        List<StreakMilestone> milestone = milestoneRepository.findAllByOrderByDaysRequiredAsc();
        List<Long> achieve = achievementRepository.findMilestoneIdsByUserId(user.getId());
        Locale locale = userService.getUserLocale(user);

        StringBuilder message = new StringBuilder(
            messageSource.getMessage("exercise.all.achievements.earned", null, locale)
        );

        for (StreakMilestone m : milestone) {
            if (!achieve.contains(m.getId()) && m.getDaysRequired() > currentStreak) {
                int daysToNext = m.getDaysRequired() - currentStreak;
                message.setLength(0);
                message.append("\n").append(messageSource.getMessage(
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
            summary.forEach(exercise -> report.append(String.format("%s - %d%n", exercise.getExerciseType(), exercise.getTotalCount())));
        } else {
            report.append(messageSource.getMessage(
                "exercise.no.workouts.found", null, locale
            ));
        }
        return report.toString();
    }

    private void appendHeader(StringBuilder sb, LocalDate start, LocalDate end, Locale locale) {
        if (start.equals(end)) {
            sb.append(messageSource.getMessage(
                "exercise.progress.for.date",
                new Object[]{start.format(ExerciseService.DATE_FORMATTER)},
                locale
            )).append("\n");
        } else {
            sb.append(messageSource.getMessage(
                "exercise.progress.period",
                new Object[]{start.format(ExerciseService.DATE_FORMATTER), end.format(ExerciseService.DATE_FORMATTER)},
                locale
            )).append("\n");
        }
    }

    private void verifyDates(LocalDate startDate, LocalDate endDate, Locale locale) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException(
                messageSource.getMessage("exercise.error.invalid.dates", null, locale)
            );
        }
    }
}
