package com.github.sportbot.controller;

import com.github.sportbot.service.StreakService;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

/**
 * Контроллер для работы со стриками (сериями дней подряд с тренировками).
 */
@RestController
@RequestMapping("/api/v1/streak")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;
    private final UserService userService;

    /**
     * Получает информацию о стрике пользователя.
     *
     * @param telegramId Telegram ID пользователя
     * @param lang язык (по умолчанию ru)
     * @return форматированная строка со стриком
     */
    @GetMapping
    public String getStreak(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId,

            @RequestParam(required = false, defaultValue = "ru")
            @Parameter(example = "ru")
            String lang
    ) {
        var user = userService.getUserByTelegramId(telegramId);
        Locale locale = Locale.forLanguageTag(lang);
        return streakService.getStreakInfo(user, locale);
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
     *
     * @param telegramId
     * @return результат сохранения Streak за Ton
     */
    @PostMapping("/save")
    public String saveStreak(@Parameter(example = "1000001") @NotNull
                             Long telegramId){
        String message = streakService.saveStreak(telegramId);
        return message;
    }
}