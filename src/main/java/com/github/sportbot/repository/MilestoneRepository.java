package com.github.sportbot.repository;

import com.github.sportbot.model.StreakMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @deprecated Use {@link AchievementDefinitionRepository} instead.
 * This repository is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated(since = "2026-04-06", forRemoval = true)
@Repository
public interface MilestoneRepository extends JpaRepository<StreakMilestone, Long> {

    @Query("""
    SELECT sm
    FROM StreakMilestone sm
    WHERE sm.daysRequired <= :days
""")
    List<StreakMilestone> findByDaysRequiredLessThanEqual(@Param("days")Integer days);

    List<StreakMilestone> findAllByOrderByDaysRequiredAsc();
}