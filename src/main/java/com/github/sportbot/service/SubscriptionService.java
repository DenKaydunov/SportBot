package com.github.sportbot.service;

import com.github.sportbot.model.*;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final org.springframework.context.MessageSource messageSource;

    @Transactional
    public String subscribe(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new IllegalArgumentException("Cannot subscribe to yourself");
        }
        User follower = userService.getUserByTelegramId(followerId);
        User following = userService.getUserByTelegramId(followingId);

        String messageKey;
        if (!subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            Subscription subscription = Subscription.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            subscriptionRepository.save(subscription);
            notificationService.notifySubscription(follower, following);
            messageKey = "subscription.subscribed";
        } else {
            messageKey = "subscription.already_subscribed";
        }
        return messageSource.getMessage(
                messageKey,
                new Object[]{following.getFullName()},
                java.util.Locale.forLanguageTag("ru-RU")
        );
    }

    @Transactional
    public String unsubscribe(Long followerId, Long followingId) {
        User follower = userService.getUserByTelegramId(followerId);
        User following = userService.getUserByTelegramId(followingId);

        String messageKey;
        if (subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            subscriptionRepository.deleteByFollowerAndFollowing(follower, following);
            messageKey = "subscription.unsubscribed";
        } else {
            messageKey = "subscription.not_subscribed";
        }
        return messageSource.getMessage(
                messageKey,
                new Object[]{following.getFullName()},
                java.util.Locale.forLanguageTag("ru-RU")
        );
    }

    @Transactional(readOnly = true)
    public List<User> getFollowing(Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        return subscriptionRepository.findByFollower(user).stream()
                .map(Subscription::getFollowing)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getFollowers(Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        return subscriptionRepository.findByFollowing(user).stream()
                .map(Subscription::getFollower)
                .toList();
    }

    public String compareProgress(Long telegramId, Long targetTelegramId, String exerciseCode) {
        User user = userService.getUserByTelegramId(telegramId);
        User target = userService.getUserByTelegramId(targetTelegramId);
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);

        int userTotal = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
        int targetTotal = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(target, exerciseType);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Сравнение прогресса по категории: %s:%n", exerciseType.getTitle()));
        sb.append(String.format("Ты: %d%n", userTotal));
        sb.append(String.format("%s: %d%n", target.getFullName(), targetTotal));

        if (userTotal > targetTotal) {
            sb.append(String.format("Ты впереди на %d!", userTotal - targetTotal));
        } else if (targetTotal > userTotal) {
            sb.append(String.format("%s позади на %d. Догоняй!", target.getFullName(), targetTotal - userTotal));
        } else {
            sb.append("У вас равный результат!");
        }

        return sb.toString();
    }
}
