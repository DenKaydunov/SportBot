package com.github.sportbot.controller;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UpdateLanguageRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.model.User;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "API для управления пользователями")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрирует нового пользователя в системе"
    )
    @PostMapping
    public UserResponse registerUser(@RequestBody RegistrationRequest request) {
        return userService.registerUser(request);
    }

    @Operation(
            summary = "Получить текущий язык пользователя",
            description = "Возвращает код языка интерфейса пользователя (ru, en или uk)"
    )
    @GetMapping("/{telegramId}/locale")
    public String getUserLocale(
            @Parameter(description = "Telegram ID пользователя", required = true, example = "1000001")
            @PathVariable Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        Locale locale = userService.getUserLocale(user);
        return locale.getLanguage();
    }

    @Operation(
            summary = "Обновить язык пользователя",
            description = "Обновляет язык интерфейса для указанного пользователя. Поддерживаются языки: ru (русский), en (английский), uk (украинский)"
    )
    @PatchMapping("/{telegramId}/locale")
    public String updateUserLanguage(
            @Parameter(description = "Telegram ID пользователя", required = true, example = "1000001")
            @PathVariable Long telegramId,
            @RequestBody @Valid UpdateLanguageRequest request) {
        return userService.updateUserLanguage(telegramId, request);
    }

    @Operation(
            summary = "Отписать пользователя от уведомлений",
            description = "Отписывает пользователя от получения уведомлений"
    )
    @PostMapping("/unsubscribe/{telegramId}")
    public String unsubscribeUser(
            @Parameter(description = "Telegram ID пользователя", required = true, example = "1000001")
            @PathVariable Long telegramId){
        return userService.unsubscribeUser(telegramId);
    }
}
