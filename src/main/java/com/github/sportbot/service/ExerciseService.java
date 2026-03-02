package com.github.sportbot.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import com.github.sportbot.model.*;
import com.github.sportbot.repository.AchievementRepository;
import com.github.sportbot.repository.MilestoneRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExercisePeriodProjection;
import com.github.sportbot.repository.ExercisePeriodRepository;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserRepository;

import lombok.RequiredArgsConstructor;
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
    private final ExercisePeriodRepository dayRepository;


    @Transactional
    public String saveExerciseResult(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(req);

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
        achievementService.checkStreakMilestones(req.telegramId());
        
        // Перезагружаем пользователя для получения обновленных данных стрика
        user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        int total = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);

        String message = messageSource.getMessage("workout.reps_recorded",
                new Object[]{exerciseType.getTitle(), req.count(), total},
                Locale.forLanguageTag("ru-RU"));
        String rankMessage = rankService.assignRankIfEligible(user, exerciseType, total);
        
        // Добавляем информацию о стрике, если он изменился
        String streakMessage = getStreakUpdateMessage(user, exercise.getDate());

        String milestone = getAchievementUpdateMessage(user);
        
        return message + rankMessage + streakMessage + milestone;
    }

    public int getTotalReps(User user, ExerciseTypeEnum exerciseCode) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode.getType());
        return exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
    }

    /**
     * Получает сообщение об обновлении стрика, если стрик изменился.
     */
    private String getStreakUpdateMessage(User user, LocalDate workoutDate) {
        LocalDate lastWorkoutDate = user.getLastWorkoutDate();

        // Если это первая тренировка или стрик увеличился
        if (lastWorkoutDate == null || (workoutDate.equals(LocalDate.now()) && lastWorkoutDate != null && lastWorkoutDate.equals(LocalDate.now()
                .minusDays(1)))) {

            int currentStreak = user.getCurrentStreak();
            if (currentStreak > 1) {
                return messageSource.getMessage("workout.streak_updated", new Object[] { currentStreak }, Locale.forLanguageTag("ru-RU"));
            }
        }

        return "";
    }

    private String getAchievementUpdateMessage(User user){
        int currentStreak = user.getCurrentStreak();

        List<StreakMilestone> milestone = milestoneRepository.findAllByOrderByDaysRequiredAsc();
        List<Integer> achieve = achievementRepository.findMilestoneIdsByUserId(user.getId());

        // Определяем milestone, который пользователь получает сейчас
        Optional<StreakMilestone> justAchieved = milestone.stream()
                .filter(m -> !achieve.contains(m.getId()))
                .filter(m -> m.getDaysRequired() == currentStreak)
                .findFirst();

        // Определяем следующий milestone
        Optional<StreakMilestone> nextMilestone = milestone.stream()
                .filter(m -> !achieve.contains(m.getId()))
                .filter(m -> m.getDaysRequired() > currentStreak)
                .findFirst();

        StringBuilder message = new StringBuilder();

        if (justAchieved.isPresent()){
            StreakMilestone m = justAchieved.get();
            message.append("\n🏆 Поздравляем! Достигнут milestone: ")
                    .append(m.getTitle())
                    .append(" (")
                    .append(m.getDaysRequired())
                    .append(" дней), ")
                    .append(m.getDescription())
                    .append(" - награда: ")
                    .append(m.getRewardTon())
                    .append(" Ton");
        }

        if (nextMilestone.isPresent()) {
            int daysToNext = nextMilestone.get().getDaysRequired() - currentStreak;
            message.append("\nДо следующего milestone осталось: ")
                    .append(daysToNext)
                    .append(" дней");
        } else { message.append("\nВсе milestones достигнуты!");
        }
        return message.toString();
    }
}
     * Provides user exercises for a specified date
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
        return dayRepository.getUserProgressByPeriod(telegramId, startDate, endDate);
    }

    public String progressForPeriod(
            Long telegramId,
            LocalDate startDate,
            LocalDate endDate) {
        userRepository.findByTelegramId(telegramId).orElseThrow(UserNotFoundException::new);

        LocalDate finalEndDate = (endDate == null) ? startDate : endDate;
        verifyDates(startDate, finalEndDate);

        List<ExercisePeriodProjection> summary = getUserProgress(telegramId, startDate, finalEndDate);

        StringBuilder report = new StringBuilder();
        appendHeader(report, startDate, finalEndDate);

        if (summary.isEmpty()) {
            report.append("Тренировок за этот период не найдено. 😴");
        } else {
            summary.forEach(exercise -> report.append(String.format("%s - %d%n", exercise.getExerciseType(), exercise.getTotalCount())));
        }

        return report.toString();
    }

    private void appendHeader(StringBuilder sb, LocalDate start, LocalDate end) {
        if (start.equals(end)) {
            sb.append("Твой прогресс за ").append(start.format(DATE_FORMATTER)).append(":\n");
        } else {
            sb.append("Твой прогресс с ").append(start.format(DATE_FORMATTER))
                    .append(" по ").append(end.format(DATE_FORMATTER)).append(":\n");
        }
    }

    private static void verifyDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания!");
        }
    }
}
