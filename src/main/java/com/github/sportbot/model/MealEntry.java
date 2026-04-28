package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Getter
@Setter
@Table(name = "meal_entries")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MealEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    @Column(nullable = false)
    private Float calories;

    @Column(nullable = false)
    private Float protein;

    @Column(nullable = false)
    private Float carbs;

    @Column(nullable = false)
    private Float fat;

    @Column(name = "meal_time")
    private LocalTime mealTime;

    @Column(nullable = false)
    private LocalDate date;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
