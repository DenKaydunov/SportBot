package com.github.sportbot.repository;

import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.AchievementDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    Optional<Achievement> findByAchievementDefinitionAndLanguage(
        AchievementDefinition definition,
        String language
    );

    List<Achievement> findByAchievementDefinition(AchievementDefinition definition);

    List<Achievement> findByLanguage(String language);
}
