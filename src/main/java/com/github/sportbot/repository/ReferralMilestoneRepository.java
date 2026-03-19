package com.github.sportbot.repository;

import com.github.sportbot.model.ReferralMilestone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReferralMilestoneRepository extends JpaRepository<ReferralMilestone, Long> {

    @Query("""
        SELECT rm
        FROM ReferralMilestone rm
        WHERE rm.referralsRequired <= :count
        ORDER BY rm.referralsRequired ASC
    """)
    List<ReferralMilestone> findByReferralsRequiredLessThanEqual(@Param("count") Integer count);

    List<ReferralMilestone> findAllByOrderByReferralsRequiredAsc();
}
