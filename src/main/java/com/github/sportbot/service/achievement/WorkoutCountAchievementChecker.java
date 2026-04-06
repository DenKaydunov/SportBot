package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Checker for workout count achievements.
 * Progress is based on count of unique workout dates for a specific exercise type.
 */
@Component
@RequiredArgsConstructor
public class WorkoutCountAchievementChecker implements AchievementChecker {

    private final ExerciseRecordRepository exerciseRecordRepository;

    @Override
    public AchievementCategory getCategory() {
        return AchievementCategory.WORKOUT_COUNT;
    }

    @Override
    public int calculateProgress(User user, AchievementDefinition definition) {
        if (user == null) {
            return 0;
        }

        Long workoutCount;
        if (definition.getExerciseType() != null) {
            // Count workouts for specific exercise type
            workoutCount = exerciseRecordRepository.countDistinctWorkoutDaysByUserAndExerciseType(
                    user,
                    definition.getExerciseType()
            );
        } else {
            // Count all workouts across all exercise types
            workoutCount = exerciseRecordRepository.countDistinctWorkoutDaysByUser(user);
        }

        return workoutCount != null ? workoutCount.intValue() : 0;
    }
}
