package com.github.sportbot.controller;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.model.User;
import com.github.sportbot.service.SubscriptionService;
import com.github.sportbot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final SportBot sportBot;

    /**
     * Endpoint to force the subscription menu to a specific user.
     * @param telegramId ID of the user who will receive the message
     * @param pageable page settings (for example ?page=0&size=10)
     */
    @Operation(summary = "Отправить меню подписок в Telegram")
    @PostMapping("/send-subscription-menu/{telegramId}")
    public ResponseEntity<String> triggerSubscriptionMenu(
            @PathVariable Long telegramId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("Sending subscription menu to user {}, page: {}, size: {}",
                telegramId, pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> userPage = userService.getAllUsersPaged(pageable);
        log.info("Retrieved {} users out of {} total, page {}/{}",
                userPage.getNumberOfElements(), userPage.getTotalElements(),
                userPage.getNumber() + 1, userPage.getTotalPages());
        sportBot.sendSubscriptionMenu(telegramId, userPage);
        return ResponseEntity.ok("Меню подписок успешно отправлено пользователю " + telegramId);
    }

    /**
     * Endpoint to send unsubscription menu to a specific user.
     * @param telegramId ID of the user who will receive the message
     * @param pageable page settings (for example ?page=0&size=10)
     */
    @Operation(summary = "Отправить меню отписок в Telegram")
    @PostMapping("/send-unsubscription-menu/{telegramId}")
    public ResponseEntity<String> triggerUnsubscriptionMenu(
            @PathVariable Long telegramId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        // Получаем список пользователей, на которых подписан текущий пользователь
        List<User> following = subscriptionService.getFollowing(telegramId);

        // Преобразуем в Page<UserResponse>
        List<UserResponse> userResponses = following.stream()
                .map(user -> new UserResponse("Success", user.getTelegramId(), user.getFullName()))
                .toList();

        Page<UserResponse> userPage = new org.springframework.data.domain.PageImpl<>(
                userResponses,
                pageable,
                userResponses.size()
        );

        sportBot.sendUnsubscriptionMenu(telegramId, userPage);
        return ResponseEntity.ok("Меню отписок успешно отправлено пользователю " + telegramId);
    }

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

    @Operation(summary = "Получить пагинированный список пользователей (для отладки)")
    @GetMapping("/users/paged")
    public ResponseEntity<Page<UserResponse>> getUsersPaged(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        log.info("Getting users page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<UserResponse> userPage = userService.getAllUsersPaged(pageable);
        log.info("Retrieved {} users out of {} total",
                userPage.getNumberOfElements(), userPage.getTotalElements());
        return ResponseEntity.ok(userPage);
    }
}
