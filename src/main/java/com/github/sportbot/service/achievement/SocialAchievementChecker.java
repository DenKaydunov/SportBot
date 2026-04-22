package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorsRepository;
import com.github.sportbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checker for social achievements based on follower/following counts.
 * Special handling for SOCIAL_HERO achievement: 10+ followers AND top-10 in any exercise.
 */
@Component
@RequiredArgsConstructor
public class SocialAchievementChecker implements AchievementChecker {

    private final SubscriptionRepository subscriptionRepository;
    private final CompetitorsRepository competitorsRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.SOCIAL;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || definition.getCode() == null) {
            return 0;
        }

        String code = definition.getCode();

        // Handle SOCIAL_FOLLOWING_* achievements
        if (code.startsWith("SOCIAL_FOLLOWING")) {
            Long followingCount = subscriptionRepository.countFollowingByUser(user);
            return followingCount != null ? followingCount.intValue() : 0;
        }

        // Handle SOCIAL_FOLLOWER_* achievements
        if (code.startsWith("SOCIAL_FOLLOWER")) {
            Long followerCount = subscriptionRepository.countFollowersByUser(user);
            return followerCount != null ? followerCount.intValue() : 0;
        }

        // Handle special SOCIAL_HERO achievement: 10+ followers AND top-10 in any exercise
        if ("SOCIAL_HERO".equals(code)) {
            Long followerCount = subscriptionRepository.countFollowersByUser(user);
            if (followerCount == null || followerCount < 10) {
                return 0;
            }

            // Check if user is in top-10 for any exercise type
            boolean inTop10 = isUserInTop10AnyExercise(user);
            return inTop10 ? 1 : 0;
        }

        return 0;
    }

    /**
     * Check if user is in top-10 position for any exercise type.
     * Optimized with single query to avoid N+1 problem.
     */
    private boolean isUserInTop10AnyExercise(User user) {
        Boolean result = competitorsRepository.isUserInTopNAnyExercise(user.getId(), 10);
        return result != null && result;
    }
}