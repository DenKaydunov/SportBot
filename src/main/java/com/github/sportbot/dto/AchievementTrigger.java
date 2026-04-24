package com.github.sportbot.dto;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import lombok.Builder;
import lombok.Data;

/**
 * Context object passed when checking achievements.
 * Contains information about what triggered the achievement check.
 */
@Data
@Builder
public class AchievementTrigger {

    /**
     * The user to check achievements for
     */
    private User user;

    /**
     * Type of event that triggered the check
     */
    private TriggerType type;

    /**
     * Optional: Exercise type if relevant to the trigger
     */
    private ExerciseType exerciseType;

    /**
     * Optional: Number of reps in this workout (for EXERCISE_RECORDED)
     */
    private Integer reps;

    /**
     * Types of events that can trigger achievement checks
     */
    public enum TriggerType {
        /**
         * User completed a workout
         */
        WORKOUT_COMPLETED,

        /**
         * User's streak was updated
         */
        STREAK_UPDATED,

        /**
         * New user registered with this user's referral code
         */
        REFERRAL_REGISTERED,

        /**
         * Exercise was recorded (triggers total reps and workout count achievements)
         */
        EXERCISE_RECORDED,

        /**
         * User updated their personal record for an exercise (triggers max reps achievements)
         */
        MAX_REPS_UPDATED,

        /**
         * User subscribed/unsubscribed to another user (triggers social achievements)
         */
        SUBSCRIPTION_CHANGED,

        /**
         * Leaderboard positions were recalculated (triggers leaderboard achievements)
         */
        LEADERBOARD_UPDATED,

        /**
         * Manual check (e.g., admin trigger or system maintenance)
         */
        MANUAL
    }
}
