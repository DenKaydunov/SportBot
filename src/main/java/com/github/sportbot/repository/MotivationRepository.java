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
     * Find random motivation for specified exercise type and locale.
     */
    @Query(value = """
        SELECT m FROM Motivation m
        JOIN m.exerciseType et
        WHERE et.code = :code
        AND m.locale = :locale
        ORDER BY function('RANDOM')
        LIMIT 1
        """)
    Optional<Motivation> findRandomByExerciseTypeCodeAndLocale(
            @Param("code") String code,
            @Param("locale") String locale
    );

    /**
     * Найти все мотивации для указанного типа упражнения.
     * @deprecated Use {@link #findRandomByExerciseTypeCodeAndLocale(String, String)} instead
     */
    @Deprecated(since = "0022", forRemoval = true)
    @Query(value = """
        SELECT m FROM Motivation m
        JOIN m.exerciseType et
        WHERE et.code = :code
        ORDER BY function('RANDOM')
        LIMIT 1
        """)
    Optional<Motivation> findRandomByExerciseTypeCode(@Param("code") String code);

}