package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    /**
     * Считает сумму значений в поле 'count' по пользователю и типу упражнения.
     * @param user Объект пользователя.
     * @param exerciseType Объект типа упражнения.
     * @return Общая сумма повторений.
     */
    @Query("SELECT COALESCE(SUM(w.count), 0) FROM ExerciseRecord w WHERE w.user = :user AND w.exerciseType = :exerciseType")
    int sumTotalRepsByUserAndExerciseType(@Param("user") User user, @Param("exerciseType") ExerciseType exerciseType);

    /**
     * Находит максимальную дату тренировки пользователя до указанной даты (не включая её).
     * @param user Объект пользователя.
     * @param beforeDate Дата, до которой искать.
     * @return Optional с максимальной датой или пустой Optional.
     */
    @Query("SELECT MAX(w.date) FROM ExerciseRecord w WHERE w.user = :user AND w.date < :beforeDate")
    java.util.Optional<LocalDate> findMaxDateByUserBeforeDate(@Param("user") User user, @Param("beforeDate") LocalDate beforeDate);

    /**
     * Проверяет, есть ли тренировки у пользователя в указанную дату.
     * @param user Объект пользователя.
     * @param date Дата для проверки.
     * @return true, если есть тренировки в эту дату.
     */
    @Query("SELECT COUNT(w) > 0 FROM ExerciseRecord w WHERE w.user = :user AND w.date = :date")
    boolean hasWorkoutOnDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("""
           SELECT et.title AS exerciseType,
           COALESCE(SUM(er.count), 0) 
           AS totalCount
           FROM ExerciseType et
           LEFT JOIN ExerciseRecord er
           ON er.exerciseType = et
           AND er.user.telegramId = :telegramId
           AND er.date BETWEEN :startDate AND :endDate
           GROUP BY et.title
""")
    List<ExercisePeriodProjection> getUserProgressByPeriod(@Param("telegramId") Long telegramId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

}
