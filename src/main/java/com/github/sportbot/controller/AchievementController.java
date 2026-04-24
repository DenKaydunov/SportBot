package com.github.sportbot.controller;

import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.model.UserAchievement;
import com.github.sportbot.service.UnifiedAchievementService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для работы с достижениями
 */
@RestController
@RequestMapping("/api/v1/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final UnifiedAchievementService unifiedAchievementService;

    /**
     * Получает информацию о полученном достижении
     *
     * @param telegramId Telegram ID пользователя
     * @return результат
     */
    @GetMapping
    public String achievementUser(@Parameter(example = "1000001") @NotNull
                                      Long telegramId){
        return unifiedAchievementService.getUserAchievementFormatted(telegramId);
    }

    /**
     * Manually check and update all achievements for a user
     *
     * @param telegramId Telegram ID пользователя
     * @return Message with count of newly unlocked achievements
     */
    @PostMapping("/check")
    public String checkAchievements(@Parameter(example = "1000001") @RequestParam @NotNull Long telegramId) {
        List<UserAchievement> newlyUnlocked = unifiedAchievementService.checkAchievementsByTelegramId(
            telegramId,
            AchievementTrigger.TriggerType.MANUAL
        );

        if (newlyUnlocked.isEmpty()) {
            return "No new achievements unlocked";
        }

        return "Unlocked " + newlyUnlocked.size() + " new achievement(s): " +
            newlyUnlocked.stream()
                .map(ua -> ua.getAchievementDefinition().getCode())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}