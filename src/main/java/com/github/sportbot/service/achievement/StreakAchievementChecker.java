package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import org.springframework.stereotype.Component;

/**
 * Checker for streak-based achievements.
 * Progress is based on user's current streak count.
 */
@Component
public class StreakAchievementChecker implements AchievementChecker {

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.STREAK;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || user.getCurrentStreak() == null) {
            return 0;
        }
        return user.getCurrentStreak();
    }
}
