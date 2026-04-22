package com.github.sportbot.service;

import com.github.sportbot.model.Achievement;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EntityLocalizationService {

    private final MessageSource messageSource;
    private final AchievementRepository achievementRepository;

    public String getExerciseTypeTitle(ExerciseType type, Locale locale) {
        return messageSource.getMessage(
                "exercise.type." + type.getCode(),
                null,
                type.getTitle(), // fallback to DB value
                locale
        );
    }

    public String getRankTitle(Rank rank, Locale locale) {
        return messageSource.getMessage(
                "rank." + rank.getCode(),
                null,
                rank.getTitle(), // fallback to DB value
                locale
        );
    }

    /**
     * Get localized title for an achievement definition
     */
    public String getAchievementTitle(AchievementDefinition definition, Locale locale) {
        String language = locale.getLanguage();

        // Пытаемся получить на запрошенном языке
        Optional<Achievement> achievement =
            achievementRepository
                .findByAchievementDefinitionAndLanguage(definition, language);

        if (achievement.isPresent()) {
            return achievement.get().getTitle();
        }

        // Fallback на русский язык
        Optional<Achievement> fallback =
            achievementRepository
                .findByAchievementDefinitionAndLanguage(definition, "ru");

        return fallback.map(Achievement::getTitle)
                       .orElse(definition.getCode());
    }

    /**
     * Get localized description for an achievement definition
     */
    public String getAchievementDescription(AchievementDefinition definition, Locale locale) {
        String language = locale.getLanguage();

        // Пытаемся получить на запрошенном языке
        Optional<Achievement> achievement =
            achievementRepository
                .findByAchievementDefinitionAndLanguage(definition, language);

        if (achievement.isPresent()) {
            return achievement.get().getDescription();
        }

        // Fallback на русский язык
        Optional<Achievement> fallback =
            achievementRepository
                .findByAchievementDefinitionAndLanguage(definition, "ru");

        return fallback.map(Achievement::getDescription)
                       .orElse("");
    }
}
