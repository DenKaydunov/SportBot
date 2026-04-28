package com.github.sportbot.repository;

import com.github.sportbot.model.NutritionProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfile, Long> {

    @Query("SELECT np FROM NutritionProfile np WHERE np.user.telegramId = :telegramId")
    Optional<NutritionProfile> findByUserTelegramId(@Param("telegramId") Long telegramId);

    @Query("SELECT CASE WHEN COUNT(np) > 0 THEN true ELSE false END FROM NutritionProfile np WHERE np.user.telegramId = :telegramId")
    boolean existsByUserTelegramId(@Param("telegramId") Long telegramId);
}
