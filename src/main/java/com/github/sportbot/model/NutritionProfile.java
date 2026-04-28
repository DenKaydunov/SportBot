package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "nutrition_profiles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class NutritionProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // User input fields
    @Column(name = "current_weight", nullable = false)
    private Float currentWeight;

    @Column(nullable = false)
    private Float height;

    @Column(name = "target_weight", nullable = false)
    private Float targetWeight;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Column(name = "dietary_restrictions", columnDefinition = "TEXT")
    private String dietaryRestrictions;

    @Enumerated(EnumType.STRING)
    @Column(name = "weight_change_speed", nullable = false)
    private WeightChangeSpeed weightChangeSpeed;

    // Calculated fields
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(name = "daily_calorie_target", nullable = false)
    private Float dailyCalorieTarget;

    @Column(name = "protein_target", nullable = false)
    private Float proteinTarget;

    @Column(name = "carbs_target", nullable = false)
    private Float carbsTarget;

    @Column(name = "fat_target", nullable = false)
    private Float fatTarget;

    @Column(name = "goal_deadline", nullable = false)
    private LocalDate goalDeadline;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
