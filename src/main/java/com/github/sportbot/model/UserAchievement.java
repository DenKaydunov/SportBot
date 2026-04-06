package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a user's progress towards and completion of an achievement.
 * Each user can have only one record per achievement (enforced by unique constraint).
 */
@Entity
@Table(name = "user_achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class UserAchievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who owns this achievement
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The achievement definition this record tracks
     */
    @ManyToOne
    @JoinColumn(name = "achievement_definition_id", nullable = false)
    private AchievementDefinition achievementDefinition;

    /**
     * Current progress towards the achievement target
     * (e.g., 15 out of 50 referrals, 750 out of 1000 push-ups)
     */
    @Column(name = "current_progress", nullable = false)
    @Builder.Default
    private Integer currentProgress = 0;

    /**
     * Date when achievement was completed (null if not yet achieved)
     */
    @Column(name = "achieved_date")
    private LocalDate achievedDate;

    /**
     * Whether user has been notified about this achievement
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean notified = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if this achievement has been completed
     */
    public boolean isAchieved() {
        return achievedDate != null;
    }

    /**
     * Calculate progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (achievementDefinition == null || achievementDefinition.getTargetValue() == 0) {
            return 0;
        }
        return Math.min(100, (currentProgress * 100) / achievementDefinition.getTargetValue());
    }
}
