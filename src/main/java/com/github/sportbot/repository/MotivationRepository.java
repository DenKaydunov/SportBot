package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Motivation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotivationRepository extends JpaRepository<Motivation, Integer> {

    /**
     * Найти все мотивации для указанного типа упражнения.
     */
    List<Motivation> findByExerciseType(ExerciseType exerciseType);
}