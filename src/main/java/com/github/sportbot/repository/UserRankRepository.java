package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRankRepository extends JpaRepository<UserRank, Long> {

    boolean existsByUserAndRank(User user, Rank rank);

    Optional<UserRank> findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(User user, ExerciseType exerciseType);

    Optional<UserRank> findTopByUserOrderByRank_ThresholdDesc(User user);
}
