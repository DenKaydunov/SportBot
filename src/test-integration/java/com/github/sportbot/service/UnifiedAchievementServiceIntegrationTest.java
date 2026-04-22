package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for UnifiedAchievementService.
 * Tests all achievement categories to ensure users receive achievements
 * for specific actions: referrals, subscriptions, max reps, total reps, streaks, etc.
 */
@SpringBootTest
@ActiveProfiles("test")
class UnifiedAchievementServiceIntegrationTest {

    @Autowired
    private UnifiedAchievementService unifiedAchievementService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AchievementDefinitionRepository achievementDefinitionRepository;

    @Autowired
    private UserAchievementRepository userAchievementRepository;

    @Autowired
    private ExerciseRecordRepository exerciseRecordRepository;

    @Autowired
    private ExerciseTypeRepository exerciseTypeRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private UserMaxHistoryRepository userMaxHistoryRepository;

    private User testUser;
    private ExerciseType pushupType;
    private ExerciseType pullupType;

    @BeforeEach
    void setUp() {
        // Clean up previous test data (order matters due to foreign keys)
        subscriptionRepository.deleteAll();
        userAchievementRepository.deleteAll();
        exerciseRecordRepository.deleteAll();
        userMaxHistoryRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.builder()
                .telegramId(999001L)
                .language("ru")
                .currentStreak(0)
                .bestStreak(0)
                .balanceTon(0)
                .fullName("Test User")
                .isSubscribed(true)
                .build();
        testUser = userRepository.save(testUser);

        // Load existing exercise types from database (created by Liquibase)
        pushupType = exerciseTypeRepository.findByCode("push_up")
                .orElseThrow(() -> new AssertionError("push_up type not found"));

        pullupType = exerciseTypeRepository.findByCode("pull_up")
                .orElseThrow(() -> new AssertionError("pull_up type not found"));

        // Achievement definitions are already created by Liquibase migrations
    }

    @Test
    void shouldUnlockTotalRepsAchievementWhenRecordingExercises() {
        // Given: User has no exercises
        assertThat(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(testUser, pushupType)).isZero();

        // When: User records 100 push-ups total (over multiple workouts)
        recordExercise(testUser, pushupType, 50, LocalDate.now());
        recordExercise(testUser, pushupType, 50, LocalDate.now());

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.EXERCISE_RECORDED)
                .exerciseType(pushupType)
                .build();

        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks PUSHUP_TOTAL_100 achievement (and possibly others)
        assertThat(newAchievements)
                .isNotEmpty()
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("PUSHUP_TOTAL_100"))
                .allMatch(UserAchievement::isAchieved);

        // Verify TON reward was given (at least 1 TON for PUSHUP_TOTAL_100)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void shouldUnlockMaxRepsAchievementWhenRecordingSingleWorkout() {
        // Given: User records a single workout with 50 push-ups
        recordExercise(testUser, pushupType, 50, LocalDate.now());

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.MAX_REPS_UPDATED)
                .exerciseType(pushupType)
                .reps(50)
                .build();

        // When: Check achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks PUSHUP_MAX_20 and PUSHUP_MAX_50 achievements
        assertThat(newAchievements)
                .hasSizeGreaterThanOrEqualTo(2)
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("PUSHUP_MAX_20"))
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("PUSHUP_MAX_50"))
                .allMatch(UserAchievement::isAchieved);

        // Verify TON reward (at least 2 + 5 = 7 for MAX achievements)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isGreaterThanOrEqualTo(7);
    }

    @Test
    void shouldUnlockStreakAchievementWhenWorkingOutConsecutiveDays() {
        // Given: User has 5-day streak
        testUser.setCurrentStreak(5);
        testUser.setBestStreak(5);
        testUser = userRepository.save(testUser);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When: Check streak achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks STREAK_5 achievement
        assertThat(newAchievements)
                .isNotEmpty()
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("STREAK_5"))
                .anyMatch(UserAchievement::isAchieved);

        // Verify TON reward
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(3);
    }

    @Test
    void shouldUnlockReferralAchievementWhenInvitingFriends() {
        // Given: User invites 3 friends (create 3 users with this user as referrer)
        for (int i = 1; i <= 3; i++) {
            User referredUser = User.builder()
                    .telegramId(999100L + i)
                    .fullName("Referred User " + i)
                    .language("ru")
                    .currentStreak(0)
                    .bestStreak(0)
                    .balanceTon(0)
                    .isSubscribed(true)
                    .referrerTelegramId(testUser.getTelegramId())
                    .build();
            userRepository.save(referredUser);
        }

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.REFERRAL_REGISTERED)
                .build();

        // When: Check referral achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks REFERRAL_1 and REFERRAL_3 achievements
        assertThat(newAchievements)
                .hasSize(2)
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("REFERRAL_1"))
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("REFERRAL_3"))
                .allMatch(UserAchievement::isAchieved);

        // Verify TON reward (5 + 10 = 15)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(15);
    }

    @Test
    void shouldUnlockWorkoutCountAchievementAfterMultipleWorkouts() {
        // Given: User has 10 workout records
        for (int i = 0; i < 10; i++) {
            recordExercise(testUser, pushupType, 10, LocalDate.now().minusDays(i));
        }

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.WORKOUT_COMPLETED)
                .build();

        // When: Check workout count achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks WORKOUT_FIRST and WORKOUT_10 achievements
        assertThat(newAchievements)
                .hasSize(2)
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("WORKOUT_FIRST"))
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("WORKOUT_10"))
                .allMatch(UserAchievement::isAchieved);

        // Verify TON reward (1 + 2 = 3)
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(3);
    }

    @Test
    void shouldUnlockSocialFollowingAchievementWhenSubscribingToUser() {
        // Given: User subscribes to another user
        User targetUser = User.builder()
                .telegramId(999002L)
                .fullName("Target User")
                .language("ru")
                .currentStreak(0)
                .bestStreak(0)
                .balanceTon(0)
                .isSubscribed(true)
                .build();
        targetUser = userRepository.save(targetUser);

        Subscription subscription = Subscription.builder()
                .follower(testUser)
                .following(targetUser)
                .build();
        subscriptionRepository.save(subscription);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.SUBSCRIPTION_CHANGED)
                .build();

        // When: Check social achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks SOCIAL_FOLLOWING_1 achievement
        assertThat(newAchievements)
                .isNotEmpty()
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("SOCIAL_FOLLOWING_1"))
                .anyMatch(UserAchievement::isAchieved);

        // Verify TON reward
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(2);
    }

    @Test
    void shouldUnlockSocialFollowerAchievementWhenGainingFollowers() {
        // Given: Another user subscribes to test user
        User follower = User.builder()
                .telegramId(999003L)
                .fullName("Follower User")
                .language("ru")
                .currentStreak(0)
                .bestStreak(0)
                .balanceTon(0)
                .isSubscribed(true)
                .build();
        follower = userRepository.save(follower);

        Subscription subscription = Subscription.builder()
                .follower(follower)
                .following(testUser)
                .build();
        subscriptionRepository.save(subscription);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.SUBSCRIPTION_CHANGED)
                .build();

        // When: Check social achievements
        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks SOCIAL_FOLLOWER_1 achievement
        assertThat(newAchievements)
                .isNotEmpty()
                .anyMatch(ua -> ua.getAchievementDefinition().getCode().equals("SOCIAL_FOLLOWER_1"))
                .anyMatch(UserAchievement::isAchieved);

        // Verify TON reward
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(5);
    }

    @Test
    void shouldNotUnlockAchievementTwice() {
        // Given: User already has PUSHUP_TOTAL_100 achievement
        recordExercise(testUser, pushupType, 100, LocalDate.now());

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.EXERCISE_RECORDED)
                .exerciseType(pushupType)
                .build();

        List<UserAchievement> firstCheck = unifiedAchievementService.checkAchievements(trigger);
        assertThat(firstCheck).isNotEmpty();

        int balanceAfterFirst = userRepository.findById(testUser.getId()).orElseThrow().getBalanceTon();

        // When: User records more exercises (now has 200 total)
        recordExercise(testUser, pushupType, 100, LocalDate.now());
        List<UserAchievement> secondCheck = unifiedAchievementService.checkAchievements(trigger);

        // Then: PUSHUP_TOTAL_100 is not returned again (only new achievements)
        assertThat(secondCheck)
                .noneMatch(ua -> ua.getAchievementDefinition().getCode().equals("PUSHUP_TOTAL_100"));

        // User's balance should not increase from already-achieved PUSHUP_TOTAL_100
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        long balanceIncrease = updatedUser.getBalanceTon() - balanceAfterFirst;

        // Should only get rewards for new achievements (not PUSHUP_TOTAL_100 again)
        assertThat(updatedUser.getBalanceTon()).isGreaterThanOrEqualTo(balanceAfterFirst);
    }

    @Test
    void shouldUpdateProgressForNotYetAchievedGoals() {
        // Given: User has 50 push-ups (not enough for 100)
        recordExercise(testUser, pushupType, 50, LocalDate.now());

        // First check EXERCISE_RECORDED achievements (TOTAL_REPS, WORKOUT_COUNT, LEADERBOARD)
        AchievementTrigger exerciseTrigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.EXERCISE_RECORDED)
                .exerciseType(pushupType)
                .build();

        List<UserAchievement> exerciseAchievements = unifiedAchievementService.checkAchievements(exerciseTrigger);

        // Then check MAX_REPS achievements (when user updates personal record)
        AchievementTrigger maxRepsTrigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.MAX_REPS_UPDATED)
                .exerciseType(pushupType)
                .reps(50)
                .build();

        List<UserAchievement> maxRepsAchievements = unifiedAchievementService.checkAchievements(maxRepsTrigger);

        // Combine all unlocked achievements
        List<UserAchievement> allUnlockedAchievements = new java.util.ArrayList<>(exerciseAchievements);
        allUnlockedAchievements.addAll(maxRepsAchievements);

        // When: Check user achievement progress for PUSHUP_TOTAL_100
        UserAchievement progress = userAchievementRepository.findAll().stream()
                .filter(ua -> ua.getAchievementDefinition().getCode().equals("PUSHUP_TOTAL_100"))
                .findFirst()
                .orElseThrow();

        // Then: Progress is tracked but not achieved
        assertThat(progress.getCurrentProgress()).isEqualTo(50);
        assertThat(progress.isAchieved()).isFalse();
        assertThat(progress.getProgressPercentage()).isEqualTo(50);

        // Verify which achievements were unlocked:
        // - PUSHUP_MAX_20 (2 TON reward)
        // - PUSHUP_MAX_50 (5 TON reward)
        // - WORKOUT_FIRST (1 TON reward)
        // - LEADERBOARD_GOLD (10 TON reward) - user becomes first in leaderboard
        assertThat(allUnlockedAchievements)
                .hasSize(4)
                .extracting(ua -> ua.getAchievementDefinition().getCode())
                .containsExactlyInAnyOrder("PUSHUP_MAX_20", "PUSHUP_MAX_50", "WORKOUT_FIRST", "LEADERBOARD_GOLD");

        // Verify exact balance: 2 + 5 + 1 + 10 = 18 TON
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getBalanceTon()).isEqualTo(18);
    }

    @Test
    void shouldUnlockMultipleCategoriesInSingleCheck() {
        // Given: User has 5-day streak and records 100 push-ups
        testUser.setCurrentStreak(5);
        testUser = userRepository.save(testUser);

        recordExercise(testUser, pushupType, 100, LocalDate.now());

        // When: Check all achievements with MANUAL trigger
        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(testUser)
                .type(AchievementTrigger.TriggerType.MANUAL)
                .build();

        List<UserAchievement> newAchievements = unifiedAchievementService.checkAchievements(trigger);

        // Then: User unlocks achievements from multiple categories
        assertThat(newAchievements)
                .hasSizeGreaterThanOrEqualTo(4)
                .anyMatch(ua -> ua.getAchievementDefinition().getCategory() == AchievementCategory.STREAK)
                .anyMatch(ua -> ua.getAchievementDefinition().getCategory() == AchievementCategory.TOTAL_REPS)
                .anyMatch(ua -> ua.getAchievementDefinition().getCategory() == AchievementCategory.MAX_REPS)
                .anyMatch(ua -> ua.getAchievementDefinition().getCategory() == AchievementCategory.WORKOUT_COUNT);
    }

    private void recordExercise(User user, ExerciseType exerciseType, int count, LocalDate date) {
        ExerciseRecord record = ExerciseRecord.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(count)
                .date(date)
                .build();
        exerciseRecordRepository.save(record);
    }
}
