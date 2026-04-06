package com.github.sportbot.repository;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing user achievements.
 */
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {

    /**
     * Find a specific user's achievement record
     */
    Optional<UserAchievement> findByUserAndAchievementDefinition(User user, AchievementDefinition achievementDefinition);

    /**
     * Find all achievements for a user, ordered by achieved date
     */
    @Query("""
        SELECT ua FROM UserAchievement ua
        WHERE ua.user.id = :userId
        ORDER BY ua.achievedDate DESC NULLS LAST, ua.updatedAt DESC
    """)
    List<UserAchievement> findByUserIdOrderByAchievedDate(@Param("userId") Integer userId);

    /**
     * Find all completed achievements for a user
     */
    @Query("""
        SELECT ua FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievedDate IS NOT NULL
        ORDER BY ua.achievedDate DESC
    """)
    List<UserAchievement> findCompletedByUserId(@Param("userId") Integer userId);

    /**
     * Find all in-progress achievements for a user
     */
    @Query("""
        SELECT ua FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievedDate IS NULL
        ORDER BY ua.currentProgress DESC
    """)
    List<UserAchievement> findInProgressByUserId(@Param("userId") Integer userId);

    /**
     * Find achievements by category for a user
     */
    @Query("""
        SELECT ua FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievementDefinition.category = :category
        ORDER BY ua.achievedDate DESC NULLS LAST
    """)
    List<UserAchievement> findByUserIdAndCategory(
            @Param("userId") Integer userId,
            @Param("category") AchievementCategory category
    );

    /**
     * Find achievements that need notification (achieved but not notified)
     */
    @Query("""
        SELECT ua FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievedDate IS NOT NULL
        AND ua.notified = false
        ORDER BY ua.achievedDate
    """)
    List<UserAchievement> findUnnotifiedAchievements(@Param("userId") Integer userId);

    /**
     * Count total completed achievements for a user
     */
    @Query("""
        SELECT COUNT(ua) FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievedDate IS NOT NULL
    """)
    Long countCompletedByUserId(@Param("userId") Integer userId);

    /**
     * Check if user has already achieved a specific definition
     */
    @Query("""
        SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END
        FROM UserAchievement ua
        WHERE ua.user.id = :userId
        AND ua.achievementDefinition.id = :definitionId
        AND ua.achievedDate IS NOT NULL
    """)
    Boolean hasAchieved(@Param("userId") Integer userId, @Param("definitionId") Long definitionId);
}
