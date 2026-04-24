package com.github.sportbot.dto.admin;

import com.github.sportbot.model.AchievementCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for achievement with all localizations
 * Used in Admin API for viewing and editing achievements
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementAdminDto {
    private Long id;
    private String code;
    private AchievementCategory category;
    private String emoji;
    private Integer targetValue;
    private Long exerciseTypeId;
    private Integer rewardTon;
    private Integer sortOrder;
    private Boolean isLegendary;
    private Boolean isActive;

    /**
     * Map of localizations by language code (e.g., "ru" -> {title, description})
     */
    private Map<String, AchievementLocalizationDto> localizations;
}
