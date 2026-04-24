package com.github.sportbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user's position in a specific exercise type leaderboard.
 * Used to efficiently fetch all positions in a single query.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExercisePosition {
    private Long exerciseTypeId;
    private Integer position;
    private Long total;
}
