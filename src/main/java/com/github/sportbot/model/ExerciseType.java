package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "exercise_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;
    private String title;

    @OneToMany(mappedBy = "exerciseType")
    private List<UserProgram> userPrograms;

    @OneToMany(mappedBy = "exerciseType")
    private List<WorkoutHistory> workoutHistory;

    @OneToMany(mappedBy = "exerciseType")
    private List<Motivation> motivations;

    @OneToMany(mappedBy = "exerciseType")
    private List<UserMaxHistory> maxHistory;
}
