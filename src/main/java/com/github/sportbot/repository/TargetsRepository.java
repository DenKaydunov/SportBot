package com.github.sportbot.repository;

import com.github.sportbot.model.Targets;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TargetsRepository extends JpaRepository<Targets, Long> {

    @NonNull
    List<Targets> findAll();
}
