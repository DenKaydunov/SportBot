package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Defines an achievement that can be unlocked by users.
 * This is the master list of all available achievements in the system.
 */
@Entity
@Table(name = "achievement_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class AchievementDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique code identifier for the achievement (e.g., "STREAK_30_DAYS", "PUSHUP_TOTAL_1000")
     */
    @Column(unique = true, nullable = false, length = 100)
    private String code;

    /**
     * Category of achievement (STREAK, TOTAL_REPS, REFERRAL, etc.)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AchievementCategory category;

    /**
     * Emoji icon representing the achievement
     */
    @Column(nullable = false, length = 10)
    private String emoji;

    /**
     * i18n key for the achievement title (e.g., "achievement.streak.30.title")
     */
    @Column(name = "title_key", nullable = false, length = 200)
    private String titleKey;

    /**
     * i18n key for the achievement description (e.g., "achievement.streak.30.description")
     */
    @Column(name = "description_key", nullable = false, length = 200)
    private String descriptionKey;

    /**
     * Target value to unlock this achievement (e.g., 30 days, 1000 reps, 50 referrals)
     */
    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    /**
     * Optional: Exercise type this achievement is specific to (null for non-exercise achievements)
     */
    @ManyToOne
    @JoinColumn(name = "exercise_type_id")
    private ExerciseType exerciseType;

    /**
     * TON reward when achievement is unlocked
     */
    @Column(name = "reward_ton", nullable = false)
    @Builder.Default
    private Integer rewardTon = 0;

    /**
     * Sort order for displaying achievements
     */
    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * Whether this is a legendary/special achievement
     */
    @Column(name = "is_legendary", nullable = false)
    @Builder.Default
    private Boolean isLegendary = false;

    /**
     * Whether this achievement is active and can be earned
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
