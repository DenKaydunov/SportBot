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

    @Query("SELECT SUM(wh.count) FROM ExerciseRecord wh WHERE wh.exerciseType.id = :exerciseTypeId")
    int sumAllByExerciseType(@Param("exerciseTypeId") Long exerciseTypeId);

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

    @Query(value = """
        SELECT
            wh.user_id,
            SUM(wh.count) as total,
            COALESCE(up.full_name, 'Без имени') as user_name,
            COALESCE(MAX(umh.max_value), 0) as max_value
        FROM exercise_record wh
        LEFT JOIN user_profiles up ON wh.user_id = up.user_id
        LEFT JOIN user_max_history umh ON wh.user_id = umh.user_id AND wh.exercise_type_id = umh.exercise_type_id
        WHERE wh.exercise_type_id = :exerciseTypeId
        GROUP BY wh.user_id, up.full_name
        ORDER BY total DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopUsersWithDetailsByExerciseType(@Param("exerciseTypeId") Long exerciseTypeId, @Param("limit") int limit);

    @Query(value = """
        SELECT
            wh.user_id,
            SUM(wh.count) as total,
            COALESCE(up.full_name, 'Без имени') as user_name,
            COALESCE(MAX(umh.max_value), 0) as max_value
        FROM exercise_record wh
        LEFT JOIN user_profiles up ON wh.user_id = up.user_id
        LEFT JOIN user_max_history umh ON wh.user_id = umh.user_id AND wh.exercise_type_id = umh.exercise_type_id
        WHERE wh.exercise_type_id = :exerciseTypeId
          AND wh.date >= :startDate
        GROUP BY wh.user_id, up.full_name
        ORDER BY total DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopUsersWithDetailsByExerciseTypeAndDateAfter(
        @Param("exerciseTypeId") Long exerciseTypeId,
        @Param("limit") int limit,
        @Param("startDate") LocalDate startDate
    );
}
