package com.github.sportbot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaderBoardRepository extends ExerciseRecordRepository {

    @Query(value = """
                        SELECT
                            COALESCE(u.full_name, 'Без имени')      AS user_name,
                            SUM(er.count)                           AS total,
                            COALESCE(MAX(umh.max_value), 0)         AS max_value
                        FROM exercise_record er
                        LEFT JOIN users u
                               ON er.user_id = u.id
                        LEFT JOIN user_max_history umh
                               ON er.user_id = umh.user_id
                              AND er.exercise_type_id = umh.exercise_type_id
                        LEFT JOIN user_tags uct
                               ON uct.user_id = er.user_id
                        WHERE er.exercise_type_id = :exerciseTypeId
                            AND (coalesce(:startDate, er.date) <= er.date)
                            AND (coalesce(:endDate, er.date) >= er.date)
                            AND (:tagId IS NULL OR uct.tag_id = :tagId)
            
                        GROUP BY er.user_id, u.full_name
                        ORDER BY total DESC
                        LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopUsersByExerciseTypeAndDate(
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("tagId") Long tagId,
            @Param("limit") int limit,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query("""
                SELECT COALESCE(SUM(er.count), 0)
                FROM ExerciseRecord er
                WHERE er.exerciseType.id = :exerciseTypeId
                    AND (coalesce(:startDate, er.date) <= er.date)
                    AND (coalesce(:endDate, er.date) >= er.date)
            """)
    int sumCountByExerciseTypeAndDate(
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
