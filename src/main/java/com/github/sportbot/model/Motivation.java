package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "motivation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Motivation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "exercise_type_id")
    private ExerciseType exerciseType;

    private String message;
}
