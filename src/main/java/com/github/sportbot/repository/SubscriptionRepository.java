package com.github.sportbot.repository;

import com.github.sportbot.model.Subscription;
import com.github.sportbot.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    @Query("SELECT s FROM Subscription s JOIN FETCH s.following WHERE s.follower = :follower")
    List<Subscription> findByFollower(@Param("follower") User follower);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.follower WHERE s.following = :following")
    List<Subscription> findByFollowing(@Param("following") User following);

    boolean existsByFollowerAndFollowing(User follower, User following);
    void deleteByFollowerAndFollowing(User follower, User following);
}
