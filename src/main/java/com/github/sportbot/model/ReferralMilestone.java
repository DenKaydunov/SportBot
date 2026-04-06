package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * @deprecated Use {@link AchievementDefinition} with category REFERRAL instead.
 * This class is kept for backward compatibility and will be removed in a future version.
 */
@Deprecated(since = "2026-04-06", forRemoval = true)
@Entity
@Table(name = "referral_milestone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class ReferralMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "referrals_required")
    Integer referralsRequired;

    Integer rewardTon;

    String title;

    String description;
}
