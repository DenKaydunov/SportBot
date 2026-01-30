package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompetitorsRepository extends JpaRepository<ExerciseRecord, Long> {

    /**
     * row[0] = position (Integer)
     * row[1] = user_id (Integer)
     * row[2] = full_name (String)
     * row[3] = total (Long)
     */
    @Query(
            value = """
            WITH totals AS (
                SELECT
                    er.user_id AS user_id,
                    COALESCE(u.full_name, 'Без имени') AS full_name,
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
                    ROW_NUMBER() OVER (ORDER BY total DESC, user_id ASC) AS position
                FROM totals
            ),
            me AS (
                SELECT position
                FROM ranked
                WHERE user_id = :userId
            )
            SELECT r.position, r.user_id, r.full_name, r.total
            FROM ranked r
            CROSS JOIN me
            WHERE r.position BETWEEN me.position - 2 AND me.position + 2
            ORDER BY r.position
        """,
            nativeQuery = true
    )
    List<Object[]> findCompetitors(
            @Param("userId") Integer userId,
            @Param("exerciseTypeId") Long exerciseTypeId
    );
}
