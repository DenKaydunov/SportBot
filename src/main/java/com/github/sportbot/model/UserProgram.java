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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer currentMax;

    private Integer dayNumber;
}
