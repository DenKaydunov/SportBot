package com.github.sportbot.service;

import java.time.LocalDate;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final UserRepository userRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MessageSource messageSource;
    private final ExerciseTypeService exerciseTypeService;
    private final RankService rankService;
    private final StreakService streakService;

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
        userRepository.save(user);

        // Обновляем стрик пользователя
        streakService.updateStreak(user, exercise.getDate());
        
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
        
        return message + rankMessage + streakMessage;
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
    }
