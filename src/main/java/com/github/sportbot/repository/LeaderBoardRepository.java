package com.github.sportbot.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaderBoardRepository extends ExerciseRecordRepository {

    @Query(value = """
                        SELECT
                            COALESCE(u.full_name, 'Без имени')      AS user_name,
                            SUM(er.count)                           AS total
                        FROM exercise_record er
                        LEFT JOIN users u
                               ON er.user_id = u.id
                        WHERE er.exercise_type_id = :exerciseTypeId
                            AND (coalesce(:startDate, er.date) <= er.date)
                            AND (coalesce(:endDate, er.date) >= er.date)
                            AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM user_tags uct WHERE uct.user_id = er.user_id AND uct.tag_id = :tagId))
            
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


    @Query(value = """
                        SELECT
                            COALESCE(u.full_name, 'Без имени')      AS user_name,
                            SUM(er.count)                           AS total
                        FROM exercise_record er
                        LEFT JOIN users u
                               ON er.user_id = u.id
                        WHERE er.exercise_type_id = :exerciseTypeId
                            AND (coalesce(:startDate, er.date) <= er.date)
                            AND (coalesce(:endDate, er.date) >= er.date)
                            AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM user_tags uct WHERE uct.user_id = er.user_id AND uct.tag_id = :tagId))
            
                        GROUP BY er.user_id, u.full_name
                        ORDER BY total DESC
                        LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> findTopUsersByExerciseTypeAndDatePaged(
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("tagId") Long tagId,
            @Param("limit") int limit,
            @Param("offset") int offset,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );


    @Query("""
                SELECT COALESCE(SUM(er.count), 0)
                FROM ExerciseRecord er
                WHERE er.exerciseType.id = :exerciseTypeId
                    AND (coalesce(:startDate, er.date) <= er.date)
                    AND (coalesce(:endDate, er.date) >= er.date)
                    AND (:tagId IS NULL OR EXISTS (SELECT 1 FROM UserTag uct WHERE uct.user.id = er.user.id AND uct.challengeTag.id = :tagId))
            """)
    int sumCountByExerciseTypeAndDate(
            @Param("exerciseTypeId") Long exerciseTypeId,
            @Param("tagId") Long tagId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
