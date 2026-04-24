package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checker for personal record (max reps in single workout) achievements.
 * Progress is based on maximum count for a specific exercise type across all workouts.
 */
@Component
@RequiredArgsConstructor
public class MaxRepsAchievementChecker implements AchievementChecker {

    private final ExerciseRecordRepository exerciseRecordRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.MAX_REPS;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || definition.getExerciseType() == null) {
            return 0;
        }

        return exerciseRecordRepository.findMaxRepsByUserAndExerciseType(
                user,
                definition.getExerciseType()
        );
    }
}