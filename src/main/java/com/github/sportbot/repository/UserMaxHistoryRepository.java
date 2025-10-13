package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserMaxHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserMaxHistoryRepository extends JpaRepository<UserMaxHistory, Long> {

    List<UserMaxHistory> findByUserAndExerciseType(User user, ExerciseType exerciseType);

    Optional<UserMaxHistory> findTopByUserAndExerciseTypeOrderByDateDesc(User user, ExerciseType exerciseType);
}
