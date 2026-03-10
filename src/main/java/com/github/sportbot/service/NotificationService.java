package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService implements MessageLocalizer {

    public static final String NOTIFICATION_SUBSCRIPTION = "subscription.notification";
    private final SportBot sportBot;
    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;

    public NotificationService(@Lazy SportBot sportBot, @Lazy SubscriptionService subscriptionService, MessageSource messageSource) {
        this.sportBot = sportBot;
        this.subscriptionService = subscriptionService;
        this.messageSource = messageSource;
    }

    public void notifySubscription(User follower, User following) {
        String message = localize(NOTIFICATION_SUBSCRIPTION, follower);
        sportBot.sendTgMessage(following.getTelegramId(), message);
    }

    public void notifyFollowersAboutNewRecord(User user, ExerciseType exerciseType, int maxValue) {
        String achievement = String.format("побил личный рекорд в %s: %d!", exerciseType.getTitle(), maxValue);
        notifyFollowers(user, achievement);
    }

    public void notifyFollowersAboutWorkout(User user, ExerciseType exerciseType, int count) {
        String achievement = String.format("выполнил тренировку: %s (%d)", exerciseType.getTitle(), count);
        notifyFollowers(user, achievement);
    }

    private void notifyFollowers(User user, String achievement) {
        Long userTelegramId = user.getTelegramId();
        subscriptionService.getFollowers(userTelegramId)
                .forEach(follower ->
                        sportBot.sendTgMessage(follower.getTelegramId(),
                                String.format("Твой друг %s %s", user.getFullName(), achievement)));
    }

    public String localize(String messageKey, Object user) {
        return messageSource.getMessage(
                messageKey,
                new Object[]{((User)user).getFullName()},
                java.util.Locale.forLanguageTag("ru-RU")
        );
    }
}
