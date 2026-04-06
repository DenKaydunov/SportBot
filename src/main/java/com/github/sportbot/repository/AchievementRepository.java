package com.github.sportbot.repository;

import com.github.sportbot.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @deprecated Use {@link UserAchievementRepository} instead.
 * This repository is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated(since = "2026-04-06", forRemoval = true)
public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Query("""
        SELECT a.milestone.id
        FROM Achievement a
        WHERE a.user.id = :userId
        ORDER BY a.achievedDate
    """)
    List<Long> findMilestoneIdsByUserId(@Param("userId")Integer id);

    @Query("""
        SELECT a.referralMilestone.id
        FROM Achievement a
        WHERE a.user.id = :userId
        AND a.referralMilestone IS NOT NULL
        ORDER BY a.achievedDate
    """)
    List<Long> findReferralMilestoneIdsByUserId(@Param("userId") Integer userId);

    @Query("""
        SELECT a
        FROM Achievement a
        WHERE a.user.id = :userId
        ORDER BY a.achievedDate
    """)
    List<Achievement> findByUserOrderByAchievedDate(@Param("userId")Integer id);
}
