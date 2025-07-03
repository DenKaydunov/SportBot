package com.github.sportbot.model;

import com.github.sportbot.model.exercise.Exercise;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

import java.util.List;


@Entity
@Data
public class User {

    @Id
    Long id;
    String username;
    String fullName;

    int age;
    double weight;

    @OneToMany
    List<Exercise> exercises;

}
