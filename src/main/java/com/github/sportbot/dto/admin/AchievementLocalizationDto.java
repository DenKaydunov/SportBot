package com.github.sportbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for achievement localization (title and description in specific language)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementLocalizationDto {
    private String language;
    private String title;
    private String description;
    private LocalDateTime updatedAt;
}
