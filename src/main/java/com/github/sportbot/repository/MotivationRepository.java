package com.github.sportbot.repository;

import com.github.sportbot.model.Motivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MotivationRepository extends JpaRepository<Motivation, Integer> {

    /**
     * Найти все мотивации для указанного типа упражнения.
     */
    @Query(value = """
        SELECT m FROM Motivation m
        JOIN m.exerciseType et
        WHERE et.code = :code
        ORDER BY function('RANDOM')
        LIMIT 1
        """)
    Optional<Motivation> findRandomByExerciseTypeCode(@Param("code") String code);

}