package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.UserExerciseSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    /**
     * Считает сумму значений в поле 'count' по пользователю и типу упражнения.
     * @param user Объект пользователя.
     * @param exerciseType Объект типа упражнения.
     * @return Общая сумма повторений.
     */
    @Query("SELECT COALESCE(SUM(w.count), 0) FROM ExerciseRecord w WHERE w.user = :user AND w.exerciseType = :exerciseType")
    int sumTotalRepsByUserAndExerciseType(@Param("user") User user, @Param("exerciseType") ExerciseType exerciseType);

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

    /**
     * Находит максимальную дату тренировки пользователя до указанной даты (не включая её).
     * @param user Объект пользователя.
     * @param beforeDate Дата, до которой искать.
     * @return Optional с максимальной датой или пустой Optional.
     */
    @Query("SELECT MAX(w.date) FROM ExerciseRecord w WHERE w.user = :user AND w.date < :beforeDate")
    Optional<LocalDate> findMaxDateByUserBeforeDate(@Param("user") User user, @Param("beforeDate") LocalDate beforeDate);

    @Query("""
    SELECT new com.github.sportbot.model.UserExerciseSummary(er.user, er.exerciseType, SUM(er.count))
    FROM ExerciseRecord er
    WHERE er.date BETWEEN :startDate AND :endDate
    GROUP BY er.user, er.exerciseType
""")
    List<UserExerciseSummary> getTotalForMonth(@Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

}
