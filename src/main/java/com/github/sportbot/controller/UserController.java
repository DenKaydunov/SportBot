package com.github.sportbot.controller;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UpdateLanguageRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.model.User;
import com.github.sportbot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public UserResponse registerUser(@RequestBody RegistrationRequest request) {
        return userService.registerUser(request);
    }

    @GetMapping("/{telegramId}/locale")
    public String getUserLocale(@PathVariable Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        Locale locale = userService.getUserLocale(user);
        return locale.getLanguage();
    }

    @PatchMapping("/{telegramId}/locale")
    public String updateUserLanguage(
            @PathVariable Long telegramId,
            @RequestBody @Valid UpdateLanguageRequest request) {
        return userService.updateUserLanguage(telegramId, request);
    }

    @PostMapping("/unsubscribe/{telegramId}")
    public String unsubscribeUser(@PathVariable Long telegramId){
        return userService.unsubscribeUser(telegramId);
    }
}
