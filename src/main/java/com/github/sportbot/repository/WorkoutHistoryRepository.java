package com.github.sportbot.repository;

import com.github.sportbot.model.WorkoutHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutHistoryRepository extends JpaRepository<WorkoutHistory, Long> {

    @Query("SELECT SUM(wh.count) FROM WorkoutHistory wh WHERE wh.exerciseType.id = :exerciseTypeId")
    int sumAllByExerciseType(@Param("exerciseTypeId") Long exerciseTypeId);

    @Query("SELECT SUM(wh.count) FROM WorkoutHistory wh WHERE wh.exerciseType.id = :exerciseTypeId AND wh.date >= :startDate")
    int sumAllByExerciseTypeAndDateAfter(@Param("exerciseTypeId") Long exerciseTypeId, @Param("startDate") LocalDate startDate);

    @Query(value = """
        SELECT
            wh.user_id,
            SUM(wh.count) as total,
            COALESCE(up.full_name, 'Без имени') as user_name,
            COALESCE(MAX(umh.max_value), 0) as max_value
        FROM workout_history wh
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
        FROM workout_history wh
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
