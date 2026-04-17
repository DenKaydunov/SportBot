package com.github.sportbot.service;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserAchievement;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * Service for managing user achievements.
 * Uses the new unified achievement system.
 */
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;
    private final UnifiedAchievementService unifiedAchievementService;

    /**
     * Get user achievements as formatted string.
     */
    public String getUserAchievement(Long telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
        Locale locale = getUserLocale(user);

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