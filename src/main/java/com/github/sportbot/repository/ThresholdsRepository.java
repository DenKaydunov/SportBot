package com.github.sportbot.repository;

import com.github.sportbot.model.Thresholds;
import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ThresholdsRepository extends JpaRepository<Thresholds, Long> {

    @Override
    @NonNull
    List<Thresholds> findAll();
}
