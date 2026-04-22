package com.github.sportbot.service.achievement;

import com.github.sportbot.dto.UserExercisePosition;
import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Checker for leaderboard position achievements.
 * Progress is binary: 1 if user's position <= targetValue, 0 otherwise.
 * Special handling for LEADERBOARD_DOMINATOR: requires #1 position in 4+ exercises.
 */
@Component
@RequiredArgsConstructor
public class LeaderboardAchievementChecker implements AchievementChecker {

    private final CompetitorsRepository competitorsRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.LEADERBOARD;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || definition.getCode() == null) {
            return 0;
        }

        // Fetch all positions in a single query to avoid N+1 problem
        List<UserExercisePosition> positions = competitorsRepository.findAllUserPositions(user.getId());

        if (positions.isEmpty()) {
            return 0;
        }

        String code = definition.getCode();

        // Special handling for DOMINATOR: #1 in 4+ exercises
        if ("LEADERBOARD_DOMINATOR".equals(code)) {
            return calculateDominatorProgress(positions);
        }

        // For all other leaderboard achievements, check best position across all exercises
        Integer bestPosition = positions.stream()
                .map(UserExercisePosition::getPosition)
                .min(Integer::compareTo)
                .orElse(null);

        if (bestPosition == null) {
            return 0;
        }

        // Binary achievement: 1 if position meets target, 0 otherwise
        return bestPosition <= definition.getTargetValue() ? 1 : 0;
    }

    /**
     * Calculate progress for DOMINATOR achievement.
     * Returns 1 if user is #1 in 4 or more exercises, 0 otherwise.
     * Optimized version using pre-fetched positions to avoid N+1 queries.
     */
    private int calculateDominatorProgress(List<UserExercisePosition> positions) {
        long firstPlaceCount = positions.stream()
                .filter(pos -> pos.getPosition() == 1)
                .count();

        return firstPlaceCount >= 4 ? 1 : 0;
    }
}