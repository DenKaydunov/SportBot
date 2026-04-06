package com.github.sportbot.repository;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing achievement definitions.
 */
public interface AchievementDefinitionRepository extends JpaRepository<AchievementDefinition, Long> {

    /**
     * Find achievement definition by unique code
     */
    Optional<AchievementDefinition> findByCode(String code);

    /**
     * Find all active achievements by category
     */
    List<AchievementDefinition> findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory category);

    /**
     * Find all active achievements
     */
    List<AchievementDefinition> findByIsActiveTrueOrderBySortOrder();

    /**
     * Find all active achievements for a specific exercise type
     */
    List<AchievementDefinition> findByExerciseTypeAndIsActiveTrueOrderBySortOrder(ExerciseType exerciseType);

    /**
     * Find all active achievements by category and optional exercise type
     */
    @Query("""
        SELECT ad FROM AchievementDefinition ad
        WHERE ad.category = :category
        AND ad.isActive = true
        AND (ad.exerciseType IS NULL OR ad.exerciseType = :exerciseType)
        ORDER BY ad.sortOrder
    """)
    List<AchievementDefinition> findByCategoryAndExerciseType(
            @Param("category") AchievementCategory category,
            @Param("exerciseType") ExerciseType exerciseType
    );

    /**
     * Find all legendary achievements
     */
    List<AchievementDefinition> findByIsLegendaryTrueAndIsActiveTrueOrderBySortOrder();
}
