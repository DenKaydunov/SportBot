package com.github.sportbot.repository;

import com.github.sportbot.model.Achievement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {

    @Query("""
        SELECT a.milestone.id
        FROM Achievement a
        WHERE a.user.id = :userId
        ORDER BY a.achievedDate
    """)
    List<Integer> findMilestoneIdsByUserId(@Param("userId")Integer id);

    @Query("""
        SELECT a
        FROM Achievement a
        WHERE a.user.id = :userId
        ORDER BY a.achievedDate
    """)
    List<Achievement> findByUserOrderByAchievedDate(@Param("userId")Integer id);
}