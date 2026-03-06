package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "streak_milestone")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class StreakMilestone {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "days_required")
    Integer daysRequired;

    Integer rewardTon;

    String title;

    String description;
}
