package com.github.sportbot.controller;

import com.github.sportbot.service.UnifiedAchievementService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
}