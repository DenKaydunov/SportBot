package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * @deprecated Use {@link UserAchievement} instead. This class is kept for backward compatibility
 * and will be removed in a future version.
 */
@Deprecated(since = "2026-04-06", forRemoval = true)
@Entity
@Table(name = "achievements")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class Achievement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "milestone_id")
    private StreakMilestone milestone;

    @ManyToOne
    @JoinColumn(name = "referral_milestone_id")
    private ReferralMilestone referralMilestone;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JoinColumn(name = "achieved_date")
    private LocalDate achievedDate;
}
