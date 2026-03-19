package com.github.sportbot.controller;

import com.github.sportbot.service.UserProfileService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.github.sportbot.dto.UpdateProfileRequest;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * Returns user profile formatted for Telegram display.
     *
     * @param telegramId Telegram user ID
     * @return profile with progress, rank, and achievements
     */
    @GetMapping
    public String getProfile(
            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId
    ) {
        return userProfileService.getProfile(telegramId);
    }

    @PutMapping
    public String updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        return userProfileService.updateProfile(request);
    }
}
