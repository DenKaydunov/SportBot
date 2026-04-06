package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checker for total repetitions achievements.
 * Progress is based on sum of all reps for a specific exercise type.
 */
@Component
@RequiredArgsConstructor
public class TotalRepsAchievementChecker implements AchievementChecker {

    private final ExerciseRecordRepository exerciseRecordRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.TOTAL_REPS;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null || definition.getExerciseType() == null) {
            return 0;
        }

        return exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(
                user,
                definition.getExerciseType()
        );
    }
}
