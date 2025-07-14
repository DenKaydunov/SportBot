package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "user_max_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserMaxHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer maxValue;
    private LocalDate date;
}
