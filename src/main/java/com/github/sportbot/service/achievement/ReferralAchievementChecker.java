package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checker for referral-based achievements.
 * Progress is based on count of users referred by this user.
 */
@Component
@RequiredArgsConstructor
public class ReferralAchievementChecker implements AchievementChecker {

    private final UserRepository userRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.REFERRAL;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || user.getTelegramId() == null) {
            return 0;
        }
        return userRepository.countByReferrerTelegramId(user.getTelegramId()).intValue();
    }
}
