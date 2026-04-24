package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.AchievementDefinitionRepository;
import com.github.sportbot.repository.UserAchievementRepository;
import com.github.sportbot.repository.UserRepository;
import com.github.sportbot.service.achievement.AchievementChecker;
import com.github.sportbot.service.achievement.MaxRepsAchievementChecker;
import com.github.sportbot.service.achievement.ReferralAchievementChecker;
import com.github.sportbot.service.achievement.StreakAchievementChecker;
import com.github.sportbot.service.achievement.TotalRepsAchievementChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnifiedAchievementServiceTest {

    @Mock
    private AchievementDefinitionRepository achievementDefinitionRepository;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StreakAchievementChecker streakChecker;

    @Mock
    private ReferralAchievementChecker referralChecker;

    @Mock
    private MaxRepsAchievementChecker maxRepsChecker;

    @Mock
    private TotalRepsAchievementChecker totalRepsChecker;

    @Mock
    private org.springframework.context.MessageSource messageSource;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @InjectMocks
    private UnifiedAchievementService unifiedAchievementService;

    private User user;
    private ExerciseType exerciseType;
    private AchievementDefinition streakDefinition10;
    private AchievementDefinition streakDefinition20;
    private AchievementDefinition referralDefinition3;
    private AchievementDefinition maxRepsDefinition50;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setTelegramId(100001L);
        user.setCurrentStreak(10);
        user.setBalanceTon(0);

        exerciseType = ExerciseType.builder()
                .id(1L)
                .code("pullup")
                .title("Подтягивания")
                .build();

        // Create streak achievement definitions
        streakDefinition10 = AchievementDefinition.builder()
                .id(1L)
                .code("STREAK_10_DAYS")
                .category(AchievementCategory.STREAK)
                .emoji("🔥")
                .targetValue(10)
                .rewardTon(5)
                .sortOrder(1)
                .isActive(true)
                .build();

        streakDefinition20 = AchievementDefinition.builder()
                .id(2L)
                .code("STREAK_20_DAYS")
                .category(AchievementCategory.STREAK)
                .emoji("💪")
                .targetValue(20)
                .rewardTon(10)
                .sortOrder(2)
                .isActive(true)
                .build();

        referralDefinition3 = AchievementDefinition.builder()
                .id(3L)
                .code("REFERRAL_3")
                .category(AchievementCategory.REFERRAL)
                .emoji("👥")
                .targetValue(3)
                .rewardTon(0)
                .sortOrder(1)
                .isActive(true)
                .build();

        maxRepsDefinition50 = AchievementDefinition.builder()
                .id(4L)
                .code("MAX_REPS_50_PULLUP")
                .category(AchievementCategory.MAX_REPS)
                .exerciseType(exerciseType)
                .emoji("💪")
                .targetValue(50)
                .rewardTon(10)
                .sortOrder(1)
                .isActive(true)
                .build();

        // Set up the checkers list
        List<AchievementChecker> checkers = List.of(streakChecker, referralChecker, maxRepsChecker, totalRepsChecker);
        unifiedAchievementService = new UnifiedAchievementService(
                achievementDefinitionRepository,
                userAchievementRepository,
                userRepository,
                checkers,
                messageSource,
                entityLocalizationService
        );

        // Setup checker behavior using lenient() to avoid UnnecessaryStubbingException
        lenient().when(streakChecker.getCategory()).thenReturn(AchievementCategory.STREAK);
        lenient().when(referralChecker.getCategory()).thenReturn(AchievementCategory.REFERRAL);
        lenient().when(maxRepsChecker.getCategory()).thenReturn(AchievementCategory.MAX_REPS);
        lenient().when(totalRepsChecker.getCategory()).thenReturn(AchievementCategory.TOTAL_REPS);
    }

    @Test
    void shouldUnlockStreakAchievementWhenThresholdReached() {
        // Given: User has exactly 10 days streak
        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK))
                .thenReturn(List.of(streakDefinition10));
        when(streakChecker.calculateProgress(user, streakDefinition10)).thenReturn(10);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, streakDefinition10))
                .thenReturn(Optional.empty());
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(1, newlyUnlocked.size());
        UserAchievement unlocked = newlyUnlocked.getFirst();
        assertEquals(10, unlocked.getCurrentProgress());
        assertNotNull(unlocked.getAchievedDate());
        assertEquals(LocalDate.now(), unlocked.getAchievedDate());

        // Verify TON reward was given
        assertEquals(5, user.getBalanceTon());
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotUnlockAchievementWhenBelowThreshold() {
        // Given: User has 8 days streak, but achievement requires 10
        user.setCurrentStreak(8);
        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK))
                .thenReturn(List.of(streakDefinition10));
        when(streakChecker.calculateProgress(user, streakDefinition10)).thenReturn(8);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, streakDefinition10))
                .thenReturn(Optional.empty());
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(0, newlyUnlocked.size());
        assertEquals(0, user.getBalanceTon()); // No reward given
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldUpdateProgressForInProgressAchievement() {
        // Given: User already has progress towards 20-day streak
        UserAchievement existingProgress = UserAchievement.builder()
                .id(1L)
                .user(user)
                .achievementDefinition(streakDefinition20)
                .currentProgress(15)
                .achievedDate(null)
                .notified(false)
                .build();

        user.setCurrentStreak(18);
        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK))
                .thenReturn(List.of(streakDefinition20));
        when(streakChecker.calculateProgress(user, streakDefinition20)).thenReturn(18);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, streakDefinition20))
                .thenReturn(Optional.of(existingProgress));
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(0, newlyUnlocked.size()); // Not yet unlocked
        assertEquals(18, existingProgress.getCurrentProgress()); // Progress updated
        assertNull(existingProgress.getAchievedDate()); // Still not achieved
        verify(userAchievementRepository).save(existingProgress);
    }

    @Test
    void shouldNotDuplicateAlreadyUnlockedAchievement() {
        // Given: User already unlocked the 10-day streak achievement
        UserAchievement alreadyUnlocked = UserAchievement.builder()
                .id(1L)
                .user(user)
                .achievementDefinition(streakDefinition10)
                .currentProgress(10)
                .achievedDate(LocalDate.now().minusDays(5))
                .notified(true)
                .build();

        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.STREAK))
                .thenReturn(List.of(streakDefinition10));
        when(streakChecker.calculateProgress(user, streakDefinition10)).thenReturn(10);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, streakDefinition10))
                .thenReturn(Optional.of(alreadyUnlocked));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(0, newlyUnlocked.size());
        assertEquals(0, user.getBalanceTon()); // No additional reward
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldCheckReferralAchievementsWhenReferralTrigger() {
        // Given
        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.REFERRAL))
                .thenReturn(List.of(referralDefinition3));
        when(referralChecker.calculateProgress(user, referralDefinition3)).thenReturn(3);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, referralDefinition3))
                .thenReturn(Optional.empty());
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.REFERRAL_REGISTERED)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(1, newlyUnlocked.size());
        UserAchievement unlocked = newlyUnlocked.getFirst();
        assertEquals(referralDefinition3, unlocked.getAchievementDefinition());
        assertEquals(3, unlocked.getCurrentProgress());
    }

    @Test
    void shouldHandleNullUserGracefully() {
        // Given
        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(null)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        // When
        List<UserAchievement> result = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(0, result.size());
        verify(achievementDefinitionRepository, never()).findByCategoryAndIsActiveTrueOrderBySortOrder(any());
    }

    @Test
    void shouldGetCompletedAchievements() {
        // Given
        UserAchievement completed1 = UserAchievement.builder()
                .achievementDefinition(streakDefinition10)
                .achievedDate(LocalDate.now())
                .build();

        when(userAchievementRepository.findCompletedByUserId(1)).thenReturn(List.of(completed1));

        // When
        List<UserAchievement> completed = unifiedAchievementService.getCompletedAchievements(1);

        // Then
        assertEquals(1, completed.size());
        verify(userAchievementRepository).findCompletedByUserId(1);
    }

    @Test
    void shouldMarkAchievementsAsNotified() {
        // Given
        UserAchievement achievement1 = UserAchievement.builder()
                .id(1L)
                .notified(false)
                .build();
        UserAchievement achievement2 = UserAchievement.builder()
                .id(2L)
                .notified(false)
                .build();

        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        // When
        unifiedAchievementService.markAsNotified(List.of(achievement1, achievement2));

        // Then
        assertTrue(achievement1.getNotified());
        assertTrue(achievement2.getNotified());
        verify(userAchievementRepository, times(2)).save(any(UserAchievement.class));
    }

    @Test
    void shouldCheckMaxRepsAchievementsOnlyOnMaxRepsUpdatedTrigger() {
        // Given: User updates their max reps
        when(achievementDefinitionRepository.findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.MAX_REPS))
                .thenReturn(List.of(maxRepsDefinition50));
        when(maxRepsChecker.calculateProgress(user, maxRepsDefinition50)).thenReturn(50);
        when(userAchievementRepository.findByUserAndAchievementDefinition(user, maxRepsDefinition50))
                .thenReturn(Optional.empty());
        when(userAchievementRepository.save(any(UserAchievement.class))).thenAnswer(i -> i.getArgument(0));

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.MAX_REPS_UPDATED)
                .exerciseType(exerciseType)
                .reps(50)
                .build();

        // When
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievements(trigger);

        // Then
        assertEquals(1, newlyUnlocked.size());
        UserAchievement unlocked = newlyUnlocked.getFirst();
        assertEquals(maxRepsDefinition50, unlocked.getAchievementDefinition());
        assertEquals(50, unlocked.getCurrentProgress());
        assertNotNull(unlocked.getAchievedDate());

        // Verify MAX_REPS checker was called
        verify(maxRepsChecker).calculateProgress(user, maxRepsDefinition50);
        verify(achievementDefinitionRepository).findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.MAX_REPS);
    }

    @Test
    void shouldNotCheckMaxRepsAchievementsOnExerciseRecordedTrigger() {
        // Given: User records an exercise (not updating max)
        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.EXERCISE_RECORDED)
                .exerciseType(exerciseType)
                .reps(30)
                .build();

        // When
        unifiedAchievementService.checkAchievements(trigger);

        // Then: MAX_REPS category should NOT be checked
        verify(achievementDefinitionRepository, never())
                .findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.MAX_REPS);
        verify(maxRepsChecker, never()).calculateProgress(any(), any());

        // But TOTAL_REPS should be checked
        verify(achievementDefinitionRepository)
                .findByCategoryAndIsActiveTrueOrderBySortOrder(AchievementCategory.TOTAL_REPS);
    }
}
