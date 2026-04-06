package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;

/**
 * Strategy interface for calculating achievement progress.
 * Each implementation handles a specific achievement category.
 */
public interface AchievementChecker {

    /**
     * Get the category this checker handles
     */
    AchievementCategory getCategory();

    /**
     * Calculate the current progress value for a user towards an achievement.
     * For example:
     * - Streak checker returns user.getCurrentStreak()
     * - Referral checker returns count of referred users
     * - Total reps checker returns sum of all reps for the exercise type
     *
     * @param user The user to check progress for
     * @param definition The achievement definition to check
     * @return Current progress value (e.g., 15 out of 50 referrals)
     */
    int calculateProgress(User user, AchievementDefinition definition);
}
