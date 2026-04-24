package com.github.sportbot.event;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event published when a user records a workout.
 * Handled after transaction commit to notify followers.
 */
@Getter
@AllArgsConstructor
public class WorkoutRecordedEvent {
    private final User user;
    private final ExerciseType exerciseType;
    private final int count;
}
