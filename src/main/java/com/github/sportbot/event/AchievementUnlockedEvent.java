package com.github.sportbot.event;

import com.github.sportbot.model.User;
import com.github.sportbot.model.UserAchievement;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Event published when a user unlocks one or more achievements.
 * Handled after transaction commit to send notifications.
 */
@Getter
@AllArgsConstructor
public class AchievementUnlockedEvent {
    private final User user;
    private final List<UserAchievement> unlockedAchievements;
}
