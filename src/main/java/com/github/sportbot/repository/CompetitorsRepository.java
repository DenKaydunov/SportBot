package com.github.sportbot.repository;

import com.github.sportbot.dto.UserExercisePosition;
import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompetitorsRepository extends JpaRepository<ExerciseRecord, Long> {

    @Query(value = """
        WITH totals AS (
            SELECT
                er.user_id AS user_id,
                full_name,
                SUM(er.count) AS total
            FROM exercise_record er
            JOIN users u ON u.id = er.user_id
            WHERE er.exercise_type_id = :exerciseTypeId
            GROUP BY er.user_id, u.full_name
        ),
        ranked AS (
            SELECT
                user_id,
                full_name,
                total,
                ROW_NUMBER() OVER (ORDER BY total DESC, user_id) AS position
            FROM totals
        )
        SELECT
            r.position,
            r.user_id AS userId,
            r.full_name AS fullName,
            r.total
        FROM ranked r
        WHERE r.position BETWEEN
            COALESCE((SELECT position FROM ranked WHERE user_id = :userId), 3) - 2
            AND
            COALESCE((SELECT position FROM ranked WHERE user_id = :userId), 3) + 2
        ORDER BY r.position
        """, nativeQuery = true)
    List<CompetitorProjection> findCompetitors(
            @Param("userId") Integer userId,
            @Param("exerciseTypeId") Long exerciseTypeId
    );

    /**
     * Fetch user's positions across all exercise types in a single query.
     * Solves N+1 problem when checking leaderboard achievements.
     * Returns only exercise types where user has records.
     */
    @Query("""
        SELECT new com.github.sportbot.dto.UserExercisePosition(
            et.id,
            CAST(RANK() OVER (PARTITION BY et.id ORDER BY SUM(er.count) DESC) AS int),
            SUM(er.count)
        )
        FROM ExerciseRecord er
        JOIN er.exerciseType et
        WHERE er.user.id = :userId
        GROUP BY et.id
        """)
    List<UserExercisePosition> findAllUserPositions(@Param("userId") Integer userId);

    /**
     * Check if user is in top N positions for any exercise type.
     * Returns true if user's best position across all exercises is <= topN.
     * Uses a single query to avoid N+1 problem in social achievement checking.
     */
    @Query("""
        SELECT CASE WHEN MIN(
            CAST(RANK() OVER (PARTITION BY et.id ORDER BY SUM(er.count) DESC) AS int)
        ) <= :topN THEN true ELSE false END
        FROM ExerciseRecord er
        JOIN er.exerciseType et
        WHERE er.user.id = :userId
        GROUP BY et.id
        """)
    Boolean isUserInTopNAnyExercise(@Param("userId") Integer userId, @Param("topN") int topN);
}