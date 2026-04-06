package com.github.sportbot.service;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.StreakMilestone;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.AchievementRepository;
import com.github.sportbot.repository.MilestoneRepository;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @deprecated These tests are for the deprecated AchievementService.
 * The functionality is now tested in UnifiedAchievementServiceTest.
 * Kept for reference but disabled.
 */
@Deprecated
@Disabled("Deprecated - functionality moved to UnifiedAchievementService and tested in UnifiedAchievementServiceTest")
@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    @Mock
    private AchievementRepository achievementRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MilestoneRepository milestoneRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserService userService;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @Mock
    private UnifiedAchievementService unifiedAchievementService;

    @InjectMocks
    private AchievementService achievementService;

    private User user;
    private Locale testLocale;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setTelegramId(100L);
        user.setCurrentStreak(10);
        user.setBalanceTon(0);
        user.setLanguage("ru");

        testLocale = Locale.forLanguageTag("ru");

        // Setup MessageSource mocks
        lenient().when(userService.getUserLocale(any(User.class))).thenReturn(testLocale);
        lenient().when(entityLocalizationService.getStreakMilestoneTitle(any(StreakMilestone.class), any(Locale.class)))
                .thenAnswer(inv -> ((StreakMilestone) inv.getArgument(0)).getTitle());
        lenient().when(messageSource.getMessage(eq("achievement.none.yet"), isNull(), any(Locale.class)))
                .thenReturn("У тебя ещё нет достижений.");
        lenient().when(messageSource.getMessage(eq("achievement.list.header"), isNull(), any(Locale.class)))
                .thenReturn("🏆 Твои достижения:");
        lenient().when(messageSource.getMessage(eq("achievement.list.item.streak"), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "• " + args[0] + " (" + args[1] + " дней) - получено: " + args[2];
                });
    }

    @Test
    void shouldThrowExceptionIfUserNotFound() {
        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> achievementService.checkStreakMilestones(100L));
    }

    @Test
    void shouldGrantNewMilestone() {
        StreakMilestone milestone = new StreakMilestone();
        milestone.setId(1L);
        milestone.setDaysRequired(5);
        milestone.setRewardTon(10);

        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.of(user));

        when(milestoneRepository.findByDaysRequiredLessThanEqual(10))
                .thenReturn(List.of(milestone));

        when(achievementRepository.findByUserOrderByAchievedDate(user.getId()))
                .thenReturn(Collections.emptyList());

        achievementService.checkStreakMilestones(100L);

        verify(achievementRepository).save(any(Achievement.class));
        verify(userRepository).save(user);

        assertEquals(10, user.getBalanceTon());
    }

    @Test
    void shouldNotGrantAlreadyAchievedMilestone() {
        StreakMilestone milestone = new StreakMilestone();
        milestone.setId(1L);
        milestone.setDaysRequired(5);
        milestone.setRewardTon(10);

        Achievement achievement = new Achievement();
        achievement.setMilestone(milestone);

        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.of(user));

        when(milestoneRepository.findByDaysRequiredLessThanEqual(10))
                .thenReturn(List.of(milestone));

        when(achievementRepository.findByUserOrderByAchievedDate(user.getId()))
                .thenReturn(List.of(achievement));

        achievementService.checkStreakMilestones(100L);

        verify(achievementRepository, never()).save(any());
    }

    @Test
    void shouldGrantMultipleMilestones() {
        StreakMilestone milestone1 = new StreakMilestone();
        milestone1.setId(1L);
        milestone1.setDaysRequired(5);
        milestone1.setRewardTon(10);

        StreakMilestone milestone2 = new StreakMilestone();
        milestone2.setId(2L);
        milestone2.setDaysRequired(10);
        milestone2.setRewardTon(20);

        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.of(user));

        when(milestoneRepository.findByDaysRequiredLessThanEqual(10))
                .thenReturn(List.of(milestone1, milestone2));

        when(achievementRepository.findByUserOrderByAchievedDate(user.getId()))
                .thenReturn(Collections.emptyList());

        achievementService.checkStreakMilestones(100L);

        verify(achievementRepository, times(2)).save(any(Achievement.class));
        assertEquals(30, user.getBalanceTon());
    }

    @Test
    void shouldReturnNoAchievementsMessage() {
        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.of(user));

        when(achievementRepository.findByUserOrderByAchievedDate(user.getId()))
                .thenReturn(Collections.emptyList());

        String result = achievementService.getUserAchievement(100L);

        assertEquals("У тебя ещё нет достижений.", result);
    }

    @Test
    void shouldReturnAchievementsList() {
        StreakMilestone milestone = new StreakMilestone();
        milestone.setTitle("5 дней подряд");
        milestone.setDaysRequired(5);

        Achievement achievement = new Achievement();
        achievement.setMilestone(milestone);
        achievement.setAchievedDate(LocalDate.of(2026, 3, 1));

        when(userRepository.findByTelegramId(100L))
                .thenReturn(Optional.of(user));

        when(achievementRepository.findByUserOrderByAchievedDate(user.getId()))
                .thenReturn(List.of(achievement));

        String result = achievementService.getUserAchievement(100L);

        assertTrue(result.contains("5 дней подряд"));
        assertTrue(result.contains("5 дней"));
        assertTrue(result.contains("2026-03-01"));
    }
}