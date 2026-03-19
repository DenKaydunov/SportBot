package com.github.sportbot.repository;

import com.github.sportbot.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByTelegramId(@NotNull Long telegramId);

    boolean existsByTelegramIdAndIsSubscribedTrue(Long telegramId);

    List<User> findAllByIsSubscribedTrue();

    @Query("""
        SELECT COUNT(u)
        FROM User u
        WHERE u.referrerTelegramId = :referrerTelegramId
    """)
    Integer countByReferrerTelegramId(@Param("referrerTelegramId") Integer referrerTelegramId);
}

