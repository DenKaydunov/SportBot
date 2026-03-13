package com.github.sportbot.service;

import com.github.sportbot.exception.SubscriptionException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SubscriptionService implements MessageLocalizer {

    public static final String SUBSCRIBED = "subscription.message.success";
    public static final String ALREADY_SUBSCRIBED = "subscription.already_subscribed";
    public static final String UNSUBSCRIBED = "unsubscription.message.success";
    public static final String NOT_SUBSCRIBED = "subscription.not_subscribed";

    private final SubscriptionRepository subscriptionRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final org.springframework.context.MessageSource messageSource;
    private final UserLocaleService localeService;


    @Transactional
    public String subscribe(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new SubscriptionException();
        }

        User follower = userService.getUserByTelegramId(followerId);
        User following = userService.getUserByTelegramId(followingId);

        String message;
        if (!subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            Subscription subscription = Subscription.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            subscriptionRepository.save(subscription);
            notificationService.notifySubscription(follower, following);
            message = localize(SUBSCRIBED, following);
        } else {
            message = localize(ALREADY_SUBSCRIBED, following);
        }
        return message;
    }

    @Transactional
    public String subscribe(Long followerId, String followerName, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new SubscriptionException();
        }

        User follower = userService.getOrCreateUser(followerId, followerName);
        User following = userService.getUserByTelegramId(followingId);

        String message;
        if (!subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            Subscription subscription = Subscription.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            subscriptionRepository.save(subscription);
            notificationService.notifySubscription(follower, following);
            message = localize(SUBSCRIBED, following);
        } else {
            message = localize(ALREADY_SUBSCRIBED, following);
        }
        return message;
    }

    @Transactional
    public String unsubscribe(Long followerId, Long followingId) {
        User follower = userService.getUserByTelegramId(followerId);
        User following = userService.getUserByTelegramId(followingId);

        String message;
        if (subscriptionRepository.existsByFollowerAndFollowing(follower, following)) {
            subscriptionRepository.deleteByFollowerAndFollowing(follower, following);
            message = localize(UNSUBSCRIBED, following);
        } else {
            message = localize(NOT_SUBSCRIBED, following);
        }
        return message;
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

    public String localize(String messageKey, Object user) {
        return messageSource.getMessage(
                messageKey,
                new Object[]{((User)user).getFullName()},
                localeService.getUserLocale((User)user)
        );
    }
}
