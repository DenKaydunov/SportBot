package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {

    Optional<Rank> findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(ExerciseType exerciseType, Integer threshold);

    Optional<Rank> findTopByExerciseTypeAndThresholdGreaterThanOrderByThresholdAsc(ExerciseType exerciseType, Integer threshold);

    boolean existsByExerciseType(ExerciseType exerciseType);
}
