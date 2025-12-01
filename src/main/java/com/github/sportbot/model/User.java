package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;
    @Column(name = "referrer_telegram_id")
    private Integer referrerTelegramId;
    @Column(name = "send_pulse_id")
    private String sendPulseId;
    @Column(name = "is_subscribed", nullable = false)
    @Builder.Default
    private Boolean isSubscribed = Boolean.TRUE;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "remind_time")
    private LocalTime remindTime;

    @OneToMany
    @JoinColumn(name = "user_id")
    @Builder.Default
    private List<UserEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @Builder.Default
    private List<UserProgram> programs = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @Builder.Default
    private List<ExerciseRecord> exerciseRecords = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    @Builder.Default
    private List<UserMaxHistory> maxHistory = new ArrayList<>();
}