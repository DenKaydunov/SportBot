package com.github.sportbot.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating achievement metadata (reward, active status, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AchievementUpdateRequest {
    private Integer rewardTon;
    private Boolean isActive;
    private String emoji;
    private Integer sortOrder;
}
