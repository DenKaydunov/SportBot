package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_ranks",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"user_id", "rank_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "rank_id", nullable = false)
    private Rank rank;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
