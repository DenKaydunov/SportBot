package com.github.sportbot.repository;

import com.github.sportbot.model.UserProgram;
import com.github.sportbot.model.UserProgramId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgramRepository extends JpaRepository<UserProgram, UserProgramId> {
    Optional<UserProgram> findByIdUserIdAndIdExerciseTypeId(Long userId, Long exerciseTypeId);
}
