package com.github.sportbot.service;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Сервис для управления стриками (сериями дней подряд с тренировками).
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    public static final Locale LOCALE = Locale.forLanguageTag("ru-RU");
    private final UserRepository userRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MessageSource messageSource;


    /**
     * Обновляет стрик пользователя на основе даты тренировки.
     * Логика:
     * - Если это первая тренировка - начинаем стрик с 1
     * - Если последняя тренировка была вчера - увеличиваем стрик на 1
     * - Если последняя тренировка была сегодня - стрик не меняется
     * - Если последняя тренировка была раньше вчера - сбрасываем стрик до 1
     *
     * @param user пользователь
     * @param workoutDate дата тренировки
     */
    @Transactional
    public void updateStreak(User user, LocalDate workoutDate) {
        LocalDate lastWorkoutDate = user.getLastWorkoutDate();

        // Если это первая тренировка
        if (lastWorkoutDate == null) {
            user.setCurrentStreak(1);
            user.setBestStreak(1);
            user.setLastWorkoutDate(workoutDate);
        } else if (workoutDate.equals(lastWorkoutDate)) {
            // Уже тренировался в этот день - стрик не меняется,
            // Но нужно убедиться, что стрик не равен 0 (для первой тренировки)
            if (user.getCurrentStreak() == 0) {
                user.setCurrentStreak(1);
                user.setBestStreak(1);
                userRepository.save(user);
            }
            return;
        } else if (workoutDate.isAfter(lastWorkoutDate)) {
            // Новая тренировка после последней
            long daysBetween = workoutDate.toEpochDay() - lastWorkoutDate.toEpochDay();

            if (daysBetween <= 1) {
                // Последняя тренировка была вчера или позавчера - продолжаем стрик
                int newStreak = user.getCurrentStreak() + 1;
                user.setCurrentStreak(newStreak);
                updateBestStreakIfNeeded(user, newStreak);
            } else {
                // Пропустили один или более дней - сбрасываем стрик
                user.setCurrentStreak(1);
            }
            user.setLastWorkoutDate(workoutDate);
        } else {
            // Тренировка в прошлом (до последней тренировки)
            // Пересчитываем стрик на основе предыдущих тренировок
            recalculateStreakForPastDate(user, workoutDate);
        }

        userRepository.save(user);
    }

    /**
     * Пересчитывает стрик для прошлой даты тренировки.
     * Используется когда пользователь добавляет тренировку задним числом.
     */
    private void recalculateStreakForPastDate(User user, LocalDate workoutDate) {
        // Получаем дату предыдущей тренировки (до workoutDate)
        LocalDate previousWorkoutDate = exerciseRecordRepository
                .findMaxDateByUserBeforeDate(user, workoutDate)
                .orElse(null);

        if (previousWorkoutDate != null && previousWorkoutDate.equals(workoutDate.minusDays(1))) {
            // Предыдущая тренировка была вчера или позавчера - продолжаем стрик
            // Но нужно пересчитать весь стрик от этой даты
            recalculateStreakFromDate(user, workoutDate);
        } else {
            // Нет предыдущей тренировки или она была раньше - начинаем новый стрик
            // Обновляем lastWorkoutDate только если это самая последняя дата
            LocalDate currentLastDate = user.getLastWorkoutDate();
            if (workoutDate.isAfter(currentLastDate)) {
                user.setLastWorkoutDate(workoutDate);
                user.setCurrentStreak(1);
            }
        }
    }

    /**
     * Пересчитывает стрик начиная с указанной даты.
     * Упрощенная версия - просто устанавливаем стрик в 1 для прошлых дат.
     */
    private void recalculateStreakFromDate(User user, LocalDate startDate) {
        // Для прошлых дат просто обновляем lastWorkoutDate если нужно
        LocalDate currentLastDate = user.getLastWorkoutDate();
        if (currentLastDate == null || startDate.isAfter(currentLastDate)) {
            user.setLastWorkoutDate(startDate);
            // Для прошлых дат не пересчитываем стрик, так как это сложно
            // Стрик будет пересчитан при следующей тренировке
        }
    }

    /**
     * Обновляет лучший стрик, если текущий стрик больше.
     */
    private void updateBestStreakIfNeeded(User user, int currentStreak) {
        if (currentStreak > user.getBestStreak()) {
            user.setBestStreak(currentStreak);
        }
    }

    /**
     * Получает информацию о стрике пользователя в виде строки.
     *
     * @param user пользователь
     * @return форматированная строка со стриком
     */
    public String getStreakInfo(User user) {
        int currentStreak = user.getCurrentStreak();
        int bestStreak = user.getBestStreak();
        LocalDate lastWorkoutDate = user.getLastWorkoutDate();
        LocalDate today = LocalDate.now();

        if (lastWorkoutDate == null) {
            return messageSource.getMessage(
                    "streak.no_workouts",
                    null,
                    LOCALE
            );
        }

        // Проверяем, не потерян ли стрик (если последняя тренировка была не сегодня и не вчера)
        if (!lastWorkoutDate.equals(today) && !lastWorkoutDate.equals(today.minusDays(1))) {
            // Стрик потерян
            long daysSinceLastWorkout = today.toEpochDay() - lastWorkoutDate.toEpochDay();
            Locale locale = Locale.forLanguageTag(user.getLanguage());
            return messageSource.getMessage(
                    "streak.lost",
                    new Object[]{bestStreak, daysSinceLastWorkout},
                    locale
            );
        }

        // Активный стрик
        if (currentStreak == bestStreak && currentStreak > 1) {
            // Это рекордный стрик
            return messageSource.getMessage(
                    "streak.active_record",
                    new Object[]{currentStreak},
                    LOCALE
            );
        } else {
            return messageSource.getMessage(
                    "streak.active",
                    new Object[]{currentStreak, bestStreak},
                    LOCALE
            );
        }
    }

    /**
     * Получает текущий стрик пользователя.
     *
     * @param user пользователь
     * @return текущий стрик
     */
    public int getCurrentStreak(User user) {
        return user.getCurrentStreak();
    }

    /**
     * Получает лучший стрик пользователя.
     *
     * @param user пользователь
     * @return лучший стрик
     */
    public int getBestStreak(User user) {
        return user.getBestStreak();
    }

    //TODO refactoring
    public String saveStreak(Long telegramId){
       User user = userRepository.findByTelegramId(telegramId)
               .orElseThrow(UserNotFoundException::new);

        LocalDate lastWorkoutDate = user.getLastWorkoutDate();
        LocalDate today = LocalDate.now();

       //проверяем что BalanceTon не 0
       if (user.getBalanceTon() < 1 || user.getBalanceTon() == null ){
           return "Недостаточно Ton для сохранения Streak";
       }

        if (lastWorkoutDate != null && (lastWorkoutDate.equals(today.minusDays(1)) ||
                lastWorkoutDate.equals(today.minusDays(2)))){
            //Списываем BalanceTon
            user.setBalanceTon(user.getBalanceTon() - 1);
            //Списываем устанавливаем новую дату отсчета streak
            user.setLastWorkoutDate(LocalDate.now());
            userRepository.save(user);
            return "Streak сохранён за 1 Ton";
        } else return "Streak нельзя сохранить, от последней тренировки прошло более 2-х дней.";
    }
}