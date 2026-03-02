package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/{followerId}/subscribe/{followingId}")
    public String subscribe(@PathVariable Long followerId, @PathVariable Long followingId) {
        return subscriptionService.subscribe(followerId, followingId);
    }

    @PostMapping("/{followerId}/unsubscribe/{followingId}")
    public String unsubscribe(@PathVariable Long followerId, @PathVariable Long followingId) {
        return subscriptionService.unsubscribe(followerId, followingId);
    }

    @GetMapping("/{telegramId}/following")
    public List<String> getFollowing(@PathVariable Long telegramId) {
        return subscriptionService.getFollowing(telegramId).stream()
                .map(User::getFullName)
                .toList();
    }

    @GetMapping("/{telegramId}/followers")
    public List<String> getFollowers(@PathVariable Long telegramId) {
        return subscriptionService.getFollowers(telegramId).stream()
                .map(User::getFullName)
                .toList();
    }

    @GetMapping("/{telegramId}/compare/{targetTelegramId}")
    public String compareProgress(@PathVariable Long telegramId,
                                  @PathVariable Long targetTelegramId,
                                  @RequestParam String exerciseCode) {
        return subscriptionService.compareProgress(telegramId, targetTelegramId, exerciseCode);
    }
}
