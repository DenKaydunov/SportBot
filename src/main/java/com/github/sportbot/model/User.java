package com.github.sportbot.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
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
@EntityListeners(AuditingEntityListener.class)
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
    @Column(name = "age")
    private Integer age;
    @Enumerated(EnumType.STRING)
    @Column(name = "sex")
    private Sex sex;
    @Column(name = "language", length = 10)
    private String language;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @Column(name = "remind_time")
    private LocalTime remindTime;

    @Column(name = "current_streak", nullable = false)
    @Builder.Default
    private Integer currentStreak = 0;

    @Column(name = "best_streak", nullable = false)
    @Builder.Default
    private Integer bestStreak = 0;

    @Column(name = "last_workout_date")
    private LocalDate lastWorkoutDate;

    @Column(name = "balance_ton",  nullable = false)
    @Builder.Default
    private Integer balanceTon = 0;

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