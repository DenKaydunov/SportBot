package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {

    /**
     * Считает сумму значений в поле 'count' по пользователю и типу упражнения.
     * @param user Объект пользователя.
     * @param exerciseType Объект типа упражнения.
     * @return Общая сумма повторений.
     */
    @Query("SELECT COALESCE(SUM(w.count), 0) FROM ExerciseRecord w WHERE w.user = :user AND w.exerciseType = :exerciseType")
    int sumTotalRepsByUserAndExerciseType(@Param("user") User user, @Param("exerciseType") ExerciseType exerciseType);

}
