package com.github.sportbot.service;

import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.model.ReferralMilestone;
import com.github.sportbot.model.StreakMilestone;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class EntityLocalizationService {

    private final MessageSource messageSource;

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
     * @deprecated Use {@link #getAchievementTitle(AchievementDefinition, Locale)} instead
     */
    @Deprecated
    public String getStreakMilestoneTitle(StreakMilestone milestone, Locale locale) {
        return messageSource.getMessage(
                "streak.milestone." + milestone.getId() + ".title",
                null,
                milestone.getTitle(), // fallback to DB value
                locale
        );
    }

    /**
     * @deprecated Use {@link #getAchievementDescription(AchievementDefinition, Locale)} instead
     */
    @Deprecated
    public String getStreakMilestoneDescription(StreakMilestone milestone, Locale locale) {
        return messageSource.getMessage(
                "streak.milestone." + milestone.getId() + ".description",
                null,
                milestone.getDescription(), // fallback to DB value
                locale
        );
    }

    /**
     * @deprecated Use {@link #getAchievementTitle(AchievementDefinition, Locale)} instead
     */
    @Deprecated
    public String getReferralMilestoneTitle(ReferralMilestone milestone, Locale locale) {
        return messageSource.getMessage(
                "referral.milestone." + milestone.getId() + ".title",
                null,
                milestone.getTitle(), // fallback to DB value
                locale
        );
    }

    /**
     * @deprecated Use {@link #getAchievementDescription(AchievementDefinition, Locale)} instead
     */
    @Deprecated
    public String getReferralMilestoneDescription(ReferralMilestone milestone, Locale locale) {
        return messageSource.getMessage(
                "referral.milestone." + milestone.getId() + ".description",
                null,
                milestone.getDescription(), // fallback to DB value
                locale
        );
    }

    /**
     * Get localized title for an achievement definition
     */
    public String getAchievementTitle(AchievementDefinition definition, Locale locale) {
        return messageSource.getMessage(
                definition.getTitleKey(),
                null,
                definition.getCode(), // fallback to code
                locale
        );
    }

    /**
     * Get localized description for an achievement definition
     */
    public String getAchievementDescription(AchievementDefinition definition, Locale locale) {
        return messageSource.getMessage(
                definition.getDescriptionKey(),
                null,
                "", // fallback to empty string
                locale
        );
    }
}
