package com.github.sportbot.repository;

import com.github.sportbot.model.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findByCode(@NotNull String code);
}
