package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.github.sportbot.dto.ExerciseDaySummary;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseDayRepository extends JpaRepository<ExerciseRecord, Long> {
    @Query("""
           SELECT et.title AS title,  
           COALESCE(SUM(er.count), 0) AS totalCount
           FROM ExerciseType et
           LEFT JOIN ExerciseRecord er
           ON er.exerciseType = et 
           AND er.user = :user 
           AND er.date = :date
           GROUP BY et.title
""")
    List<ExerciseDaySummaryProjection> getUserDayProgressBy(@Param("user") User user,
                                                            @Param("date") LocalDate date);

}
