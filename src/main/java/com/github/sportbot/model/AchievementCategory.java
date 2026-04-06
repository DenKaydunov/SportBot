package com.github.sportbot.model;

/**
 * Categories of achievements in the unified achievement system.
 * Each category represents a different type of accomplishment.
 */
public enum AchievementCategory {
    /**
     * Achievements based on workout streaks (consecutive days)
     */
    STREAK,

    /**
     * Achievements based on total repetitions across all time
     */
    TOTAL_REPS,

    /**
     * Achievements based on personal records (max reps in single workout)
     */
    MAX_REPS,

    /**
     * Achievements based on total number of workouts
     */
    WORKOUT_COUNT,

    /**
     * Achievements based on number of referred users
     */
    REFERRAL,

    /**
     * Social achievements (subscriptions, interactions, etc.)
     */
    SOCIAL,

    /**
     * Leaderboard position achievements
     */
    LEADERBOARD
}
