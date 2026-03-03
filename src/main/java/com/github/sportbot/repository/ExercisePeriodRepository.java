package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExercisePeriodRepository extends JpaRepository<ExerciseRecord, Long> {

    @Query("""
           SELECT et.title AS exerciseType,
           COALESCE(SUM(er.count), 0) 
           AS totalCount
           FROM ExerciseType et
           LEFT JOIN ExerciseRecord er
           ON er.exerciseType = et
           AND er.user.telegramId = :telegramId
           AND er.date BETWEEN :startDate AND :endDate
           GROUP BY et.title
""")
    List<ExercisePeriodProjection> getUserProgressByPeriod(@Param("telegramId") Long telegramId,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    @Query("""
    SELECT et.title 
    AS exerciseType, 
    COALESCE(SUM(er.count), 0) 
    AS totalCount 
    FROM ExerciseType et 
    LEFT JOIN ExerciseRecord er 
    ON er.exerciseType = et 
    AND er.user.telegramId = :telegramId 
    AND er.date = :date 
    GROUP BY et.title
    """)
    List<ExercisePeriodProjection> getUserProgressForDate(@Param("telegramId") Long telegramId,
                                                          @Param("date") LocalDate date);
}
