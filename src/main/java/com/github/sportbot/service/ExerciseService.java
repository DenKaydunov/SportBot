package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExercisePeriodProjection;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserRepository;
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
    private final NotificationService notificationService;


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

        int total = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);

        String message = messageSource.getMessage("workout.reps_recorded",
                new Object[]{exerciseType.getTitle(), req.count(), total},
                Locale.forLanguageTag("ru-RU"));
        String rankMessage = rankService.assignRankIfEligible(user, exerciseType, total);
        return message + rankMessage;
    }

    public int getTotalReps(User user, ExerciseTypeEnum exerciseCode) {
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode.getType());
        return exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
    }

    /**
     * Provides user exercises for a specified date
     * <p>
     * Твой прогресс за 25.02.2026:
     * Приседания - 0
     * Подтягивания - 20
     * Отжимания - 10
     * Пресс - 0
     *
     */
    public String progressForPeriod(
            Long telegramId,
            LocalDate startDate,
            LocalDate endDate) {
        userRepository.findByTelegramId(telegramId).orElseThrow(UserNotFoundException::new);

        LocalDate finalEndDate = (endDate == null) ? startDate : endDate;
        verifyDates(startDate, finalEndDate);

        List<ExercisePeriodProjection> summary = exerciseRecordRepository.getUserProgressByPeriod(telegramId, startDate, endDate);

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