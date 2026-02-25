package com.github.sportbot.repository;

import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseDayRepository extends JpaRepository<ExerciseRecord, Long> {
    @Query("""
           SELECT w.exerciseType,  COALESCE(SUM(w.count), 0)
           FROM ExerciseRecord w
           WHERE w.user = :user
           AND w.date = :date
           GROUP BY w.exerciseType
""")
    List<Object[]> sumByUserAndDate(@Param("user") User user,
                                     @Param("date") LocalDate date);

}
