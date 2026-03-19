package com.github.sportbot.repository;

import com.github.sportbot.model.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {

    // New methods for global ranks (exercise_type_id IS NULL)
    Optional<Rank> findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(Integer threshold);

    Optional<Rank> findTopByExerciseTypeIsNullAndThresholdGreaterThanOrderByThresholdAsc(Integer threshold);

    boolean existsByExerciseTypeIsNull();
}
