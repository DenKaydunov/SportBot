package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserAchievement;
import com.github.sportbot.repository.AchievementRepository;
import com.github.sportbot.repository.MilestoneRepository;
import com.github.sportbot.repository.ReferralMilestoneRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * @deprecated This service is being replaced by {@link UnifiedAchievementService}.
 * Some methods remain for backward compatibility but will be removed in a future version.
 */
@Service
@RequiredArgsConstructor
public class AchievementService {

    @Deprecated
    private final AchievementRepository achievementRepository;
    private final UserRepository userRepository;
    @Deprecated
    private final MilestoneRepository milestoneRepository;
    @Deprecated
    private final ReferralMilestoneRepository referralMilestoneRepository;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;

    // New unified service
    private final UnifiedAchievementService unifiedAchievementService;

    /**
     * @deprecated Use {@link UnifiedAchievementService#checkAchievementsByTelegramId(Long, AchievementTrigger.TriggerType)}
     * with TriggerType.STREAK_UPDATED instead.
     */
    @Deprecated
    @Transactional
    public void checkStreakMilestones(Long telegramId){
        // Delegate to unified service
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.STREAK_UPDATED)
                .build();

        unifiedAchievementService.checkAchievements(trigger);
    }

    /**
     * @deprecated Use {@link UnifiedAchievementService#checkAchievements(AchievementTrigger)}
     * with TriggerType.REFERRAL_REGISTERED instead.
     */
    @Deprecated
    @Transactional
    public void checkReferralMilestones(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        AchievementTrigger trigger = AchievementTrigger.builder()
                .user(user)
                .type(AchievementTrigger.TriggerType.REFERRAL_REGISTERED)
                .build();

        unifiedAchievementService.checkAchievements(trigger);
    }

    /**
     * Get user achievements as formatted string.
     * Updated to use new unified achievement system.
     */
    public String getUserAchievement(Long telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
        Locale locale = getUserLocale(user);

        // Use new unified service
        List<UserAchievement> achievements = unifiedAchievementService.getCompletedAchievements(user.getId());

        if (achievements == null || achievements.isEmpty()){
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

    private Locale getUserLocale(User user){
        String lang = user.getLanguage();
        if (!"ru".equals(lang) && !"en".equals(lang) && !"uk".equals(lang)){
            lang = "ru";
        }
        return Locale.forLanguageTag(lang);
    }
}