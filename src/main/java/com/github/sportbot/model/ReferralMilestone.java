package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

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
