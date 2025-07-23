package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_programs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProgram {

    @EmbeddedId
    private UserProgramId id;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @MapsId("exerciseTypeId")
    @JoinColumn(name = "exercise_type_id", nullable = false)
    private ExerciseType exerciseType;

    @Column(name = "current_max", nullable = false)
    private Integer currentMax;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;
}
