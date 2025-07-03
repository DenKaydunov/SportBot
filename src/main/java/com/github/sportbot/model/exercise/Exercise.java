package com.github.sportbot.model.exercise;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class Exercise {
    @Id
    Long id;
    Long userId;
    Date date;
    ExerciseType exerciseType;
    int count;
}
