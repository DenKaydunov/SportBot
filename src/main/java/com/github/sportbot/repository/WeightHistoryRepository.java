package com.github.sportbot.repository;

import com.github.sportbot.model.WeightHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeightHistoryRepository extends JpaRepository<WeightHistory, Long> {

    @Query("SELECT wh FROM WeightHistory wh WHERE wh.user.telegramId = :telegramId ORDER BY wh.date DESC")
    List<WeightHistory> findByUserTelegramIdOrderByDateDesc(@Param("telegramId") Long telegramId);

    @Query("SELECT wh FROM WeightHistory wh WHERE wh.user.telegramId = :telegramId ORDER BY wh.date DESC LIMIT 1")
    Optional<WeightHistory> findFirstByUserTelegramIdOrderByDateDesc(@Param("telegramId") Long telegramId);

    @Query("SELECT wh FROM WeightHistory wh WHERE wh.user.telegramId = :telegramId ORDER BY wh.date ASC LIMIT 1")
    Optional<WeightHistory> findFirstByUserTelegramIdOrderByDateAsc(@Param("telegramId") Long telegramId);

    @Query("""
        SELECT wh FROM WeightHistory wh
        WHERE wh.user.telegramId = :telegramId
        AND wh.date BETWEEN :startDate AND :endDate
        ORDER BY wh.date DESC
    """)
    List<WeightHistory> findByUserTelegramIdAndDateBetween(
        @Param("telegramId") Long telegramId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT wh FROM WeightHistory wh WHERE wh.user.telegramId = :telegramId AND wh.date = :date")
    Optional<WeightHistory> findByUserTelegramIdAndDate(
        @Param("telegramId") Long telegramId,
        @Param("date") LocalDate date
    );
}
