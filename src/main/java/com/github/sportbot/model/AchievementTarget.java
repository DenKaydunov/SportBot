package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "achievement_targets")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AchievementTarget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "value", nullable = false)
    private Integer value;

    @Column(name = "description", nullable = false)
    private String description;
}
