package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.*;
import java.util.*;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer telegramId;
    private String sendPulseId;
    private Boolean isSubscribed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

