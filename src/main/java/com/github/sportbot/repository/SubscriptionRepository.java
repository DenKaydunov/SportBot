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

    /**
     * Count number of users following this user (subscribers/followers).
     * Used for SOCIAL_FOLLOWER achievements.
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.following = :user")
    Long countFollowersByUser(@Param("user") User user);

    /**
     * Count number of users this user is following (subscriptions).
     * Used for SOCIAL_FOLLOWING achievements.
     */
    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.follower = :user")
    Long countFollowingByUser(@Param("user") User user);
}
