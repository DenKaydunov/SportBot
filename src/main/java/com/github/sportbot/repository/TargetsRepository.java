package com.github.sportbot.repository;

import com.github.sportbot.model.AchievementTarget;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetsRepository extends JpaRepository<AchievementTarget, Long> {
}
