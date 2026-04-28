package com.github.sportbot.repository;

import com.github.sportbot.model.MealEntry;
import com.github.sportbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MealEntryRepository extends JpaRepository<MealEntry, Long> {

    @Query("SELECT COALESCE(SUM(m.calories), 0.0) FROM MealEntry m WHERE m.user = :user AND m.date = :date")
    Float sumCaloriesByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(m.protein), 0.0) as protein,
               COALESCE(SUM(m.carbs), 0.0) as carbs,
               COALESCE(SUM(m.fat), 0.0) as fat
        FROM MealEntry m
        WHERE m.user = :user AND m.date = :date
    """)
    MacroProjection sumMacrosByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    @Query("SELECT m FROM MealEntry m WHERE m.user.telegramId = :telegramId AND m.date = :date ORDER BY m.createdAt DESC")
    List<MealEntry> findByUserTelegramIdAndDate(@Param("telegramId") Long telegramId, @Param("date") LocalDate date);

    @Query("""
        SELECT m FROM MealEntry m
        WHERE m.user.telegramId = :telegramId
        AND m.date BETWEEN :startDate AND :endDate
        ORDER BY m.date DESC, m.createdAt DESC
    """)
    List<MealEntry> findByUserTelegramIdAndPeriod(
        @Param("telegramId") Long telegramId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(DISTINCT m.date) FROM MealEntry m WHERE m.user = :user AND m.date >= :after")
    Long countDistinctMealDaysByUser(@Param("user") User user, @Param("after") LocalDate after);
}
