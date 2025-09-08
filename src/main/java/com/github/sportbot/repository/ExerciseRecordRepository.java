package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    @Query("SELECT SUM(wh.count) FROM ExerciseRecord wh WHERE wh.exerciseType.id = :exerciseTypeId")
    int sumCountByExerciseType(@Param("exerciseTypeId") Long exerciseTypeId);

    /**
     * Считает сумму значений в поле 'count' по пользователю и типу упражнения.
     * @param user Объект пользователя.
     * @param exerciseType Объект типа упражнения.
     * @return Общая сумма повторений.
     */
    @Query("SELECT SUM(w.count) FROM ExerciseRecord w WHERE w.user = :user AND w.exerciseType = :exerciseType")
    int sumTotalReps(@Param("user") User user, @Param("exerciseType") ExerciseType exerciseType);


    @Query("SELECT SUM(wh.count) FROM ExerciseRecord wh WHERE wh.exerciseType.id = :exerciseTypeId AND wh.date >= :startDate")
    int sumAllByExerciseTypeAndDateAfter(@Param("exerciseTypeId") Long exerciseTypeId, @Param("startDate") LocalDate startDate);
}
