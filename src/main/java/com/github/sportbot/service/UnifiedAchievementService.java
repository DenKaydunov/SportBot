package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserAchievement;
import com.github.sportbot.repository.AchievementDefinitionRepository;
import com.github.sportbot.repository.UserAchievementRepository;
import com.github.sportbot.repository.UserRepository;
import com.github.sportbot.service.achievement.AchievementChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Unified service for managing all types of achievements.
 * Uses strategy pattern with AchievementChecker implementations for different categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UnifiedAchievementService {

    private final AchievementDefinitionRepository achievementDefinitionRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final List<AchievementChecker> checkers;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;

    /**
     * Check and update achievements for a user based on a trigger event.
     * This method will:
     * 1. Identify relevant achievement definitions based on trigger type
     * 2. Calculate current progress using appropriate checkers
     * 3. Update or create UserAchievement records
     * 4. Award TON rewards for newly unlocked achievements
     *
     * @param trigger Context about what triggered the achievement check
     * @return List of newly unlocked achievements (for notification purposes)
     */
    @Transactional
    public List<UserAchievement> checkAchievements(AchievementTrigger trigger) {
        User user = trigger.getUser();
        if (user == null) {
            log.warn("checkAchievements called with null user");
            return List.of();
        }

        // Map checkers by category for easy lookup
        Map<AchievementCategory, AchievementChecker> checkerMap = checkers.stream()
                .collect(Collectors.toMap(AchievementChecker::getCategory, Function.identity()));

        // Determine which categories to check based on trigger type
        List<AchievementCategory> categoriesToCheck = getCategoriesToCheck(trigger.getType());

        List<UserAchievement> newlyUnlocked = new java.util.ArrayList<>();

        for (AchievementCategory category : categoriesToCheck) {
            AchievementChecker checker = checkerMap.get(category);
            if (checker == null) {
                log.warn("No checker found for category: {}", category);
                continue;
            }

            // Get all active achievements for this category
            List<AchievementDefinition> definitions = achievementDefinitionRepository
                    .findByCategoryAndIsActiveTrueOrderBySortOrder(category);

            for (AchievementDefinition definition : definitions) {
                // Calculate current progress
                int progress = checker.calculateProgress(user, definition);

                // Get or create user achievement record
                UserAchievement userAchievement = userAchievementRepository
                        .findByUserAndAchievementDefinition(user, definition)
                        .orElseGet(() -> {
                            UserAchievement newRecord = UserAchievement.builder()
                                    .user(user)
                                    .achievementDefinition(definition)
                                    .currentProgress(0)
                                    .notified(false)
                                    .build();
                            return userAchievementRepository.save(newRecord);
                        });

                // Update progress
                boolean wasAchieved = userAchievement.isAchieved();
                userAchievement.setCurrentProgress(progress);

                // Check if achievement is newly unlocked
                if (!wasAchieved && progress >= definition.getTargetValue()) {
                    userAchievement.setAchievedDate(LocalDate.now());

                    // Award TON reward
                    if (definition.getRewardTon() > 0) {
                        user.setBalanceTon(user.getBalanceTon() + definition.getRewardTon());
                        userRepository.save(user);
                        log.info("User {} unlocked achievement {} and received {} TON",
                                user.getTelegramId(), definition.getCode(), definition.getRewardTon());
                    }

                    newlyUnlocked.add(userAchievement);
                }

                userAchievementRepository.save(userAchievement);
            }
        }

        return newlyUnlocked;
    }

    /**
     * Check achievements for a user by telegram ID
     *
     * @param telegramId Telegram ID of the user
     * @param triggerType Type of event that triggered the check
     * @return List of newly unlocked achievements
     */
    @Transactional
    @SuppressWarnings("java:S6809") // Both methods are @Transactional, so this is safe
    public List<UserAchievement> checkAchievementsByTelegramId(Long telegramId, AchievementTrigger.TriggerType triggerType) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(triggerType)
                .build();

        return checkAchievements(trigger);
    }

    /**
     * Determine which achievement categories should be checked based on trigger type
     */
    private List<AchievementCategory> getCategoriesToCheck(AchievementTrigger.TriggerType triggerType) {
        return switch (triggerType) {
            case STREAK_UPDATED -> List.of(AchievementCategory.STREAK);
            case REFERRAL_REGISTERED -> List.of(AchievementCategory.REFERRAL);
            case EXERCISE_RECORDED -> List.of(
                    AchievementCategory.TOTAL_REPS,
                    AchievementCategory.MAX_REPS,
                    AchievementCategory.WORKOUT_COUNT,
                    AchievementCategory.LEADERBOARD
            );
            case WORKOUT_COMPLETED -> List.of(
                    AchievementCategory.STREAK,
                    AchievementCategory.WORKOUT_COUNT
            );
            case SUBSCRIPTION_CHANGED -> List.of(
                    AchievementCategory.SOCIAL
            );
            case LEADERBOARD_UPDATED -> List.of(
                    AchievementCategory.LEADERBOARD
            );
            case MANUAL -> List.of(
                    AchievementCategory.STREAK,
                    AchievementCategory.REFERRAL,
                    AchievementCategory.TOTAL_REPS,
                    AchievementCategory.MAX_REPS,
                    AchievementCategory.WORKOUT_COUNT,
                    AchievementCategory.SOCIAL,
                    AchievementCategory.LEADERBOARD
            );
        };
    }

    /**
     * Get all achievements for a user
     */
    public List<UserAchievement> getUserAchievements(Integer userId) {
        return userAchievementRepository.findByUserIdOrderByAchievedDate(userId);
    }

    /**
     * Get completed achievements for a user
     */
    public List<UserAchievement> getCompletedAchievements(Integer userId) {
        return userAchievementRepository.findCompletedByUserId(userId);
    }

    /**
     * Get in-progress achievements for a user
     */
    public List<UserAchievement> getInProgressAchievements(Integer userId) {
        return userAchievementRepository.findInProgressByUserId(userId);
    }

    /**
     * Get achievements that need notification
     */
    public List<UserAchievement> getUnnotifiedAchievements(Integer userId) {
        return userAchievementRepository.findUnnotifiedAchievements(userId);
    }

    /**
     * Mark achievements as notified
     */
    @Transactional
    public void markAsNotified(List<UserAchievement> achievements) {
        achievements.forEach(a -> {
            a.setNotified(true);
            userAchievementRepository.save(a);
        });
    }

    /**
     * Get user achievements as formatted string by telegram ID.
     *
     * @param telegramId Telegram ID пользователя
     * @return форматированная строка с достижениями
     */
    public String getUserAchievementFormatted(Long telegramId) {
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
        Locale locale = getUserLocale(user);

        List<UserAchievement> achievements = getCompletedAchievements(user.getId());

        if (achievements == null || achievements.isEmpty()) {
            return messageSource.getMessage("achievement.none.yet", null, locale);
        }

        StringBuilder result = new StringBuilder(
            messageSource.getMessage("achievement.list.header", null, locale)
        ).append("\n");

        achievements.forEach(ua -> {
            String title = entityLocalizationService.getAchievementTitle(ua.getAchievementDefinition(), locale);
            result.append(
                messageSource.getMessage(
                    "achievement.list.item.unified",
                    new Object[]{
                        ua.getAchievementDefinition().getEmoji(),
                        title,
                        ua.getAchievedDate()
                    },
                    locale
                )
            ).append("\n");
        });

        return result.toString();
    }

    private Locale getUserLocale(User user) {
        String lang = user.getLanguage();
        if (!"ru".equals(lang) && !"en".equals(lang) && !"uk".equals(lang)) {
            lang = "ru";
        }
        return Locale.forLanguageTag(lang);
    }
}
