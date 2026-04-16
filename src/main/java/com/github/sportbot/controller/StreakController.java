package com.github.sportbot.controller;

import com.github.sportbot.service.StreakService;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * Контроллер для работы со стриками (сериями дней подряд с тренировками).
 */
@RestController
@RequestMapping("/api/v1/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;
    private final UserService userService;

    /**
     * Получает информацию о стрике пользователя.
     *
     * @param telegramId Telegram ID пользователя
     * @return форматированная строка со стриком
     */
    @GetMapping
    public String getStreak(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId
    ) {
        var user = userService.getUserByTelegramId(telegramId);
        return streakService.getStreakInfo(user);
    }

    /**
     * Получает текущий стрик пользователя.
     *
     * @param telegramId Telegram ID пользователя
     * @return текущий стрик
     */
    @GetMapping("/current")
    public int getCurrentStreak(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId
    ) {
        var user = userService.getUserByTelegramId(telegramId);
        return streakService.getCurrentStreak(user);
    }

    /**
     * Получает лучший стрик пользователя.
     *
     * @param telegramId Telegram ID пользователя
     * @return лучший стрик
     */
    @GetMapping("/best")
    public int getBestStreak(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId
    ) {
        var user = userService.getUserByTelegramId(telegramId);
        return streakService.getBestStreak(user);
    }

    /**
     * Попытка сохранить страйк используя валюту
     * @param telegramId Telegram ID пользователя
     * @return результат сохранения Streak за Ton
     */
    @PostMapping("/save")
    public String saveStreak(@Parameter(example = "1000001") @NotNull
                             Long telegramId){
        return streakService.saveStreak(telegramId);
    }
}