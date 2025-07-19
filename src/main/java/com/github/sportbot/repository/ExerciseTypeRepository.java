package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
    Optional<ExerciseType> findByCode(String code);
}
