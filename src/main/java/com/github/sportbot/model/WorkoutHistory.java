package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;

@Entity
@Table(name = "workout_history")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkoutHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "exercise_type_id", nullable = false)
    private ExerciseType exerciseType;

    private Integer count;

    @CreationTimestamp
    private LocalDate date;
}
