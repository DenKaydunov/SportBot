package com.github.sportbot.controller;

import com.github.sportbot.service.AchievementService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с достижениями
 */
@RestController
@RequestMapping("/api/v1/milestones")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * Получает информацию о поллученом достижении
     *
     * @param telegramId
     * @return результат
     */
    @GetMapping("/achievement")
    public String achievementUser(@Parameter(example = "1000001") @NotNull
                                      Long telegramId){
        return achievementService.getUserAchievement(telegramId);
    }
}