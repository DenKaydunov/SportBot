package com.github.sportbot.controller;

import com.github.sportbot.service.UserProfileService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Returns user profile formatted for Telegram display.
     *
     * @param telegramId Telegram user ID
     * @param lang optional language code (e.g. ru, en)
     * @return profile with progress, rank, and achievements
     */
    @GetMapping
    public String getProfile(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId,

            @RequestParam(required = false, defaultValue = "ru")
            @Parameter(example = "ru")
            String lang
    ) {
        return userProfileService.getProfile(telegramId, lang);
    }
}
