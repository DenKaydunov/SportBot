package com.github.sportbot.repository;

import com.github.sportbot.model.Targets;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TargetsRepository extends JpaRepository<Targets, Long> {
}
