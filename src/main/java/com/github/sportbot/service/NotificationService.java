package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@Slf4j
public class NotificationService implements MessageLocalizer {

    public static final String NOTIFICATION_SUBSCRIPTION = "subscription.notification";
    private final SportBot sportBot;
    private final SubscriptionService subscriptionService;
    private final MessageSource messageSource;
    private final UserService userService;
    private final EntityLocalizationService entityLocalizationService;

    public NotificationService(@Lazy SportBot sportBot,
                               @Lazy SubscriptionService subscriptionService,
                               MessageSource messageSource,
                               UserService userService,
                               EntityLocalizationService entityLocalizationService) {
        this.sportBot = sportBot;
        this.subscriptionService = subscriptionService;
        this.messageSource = messageSource;
        this.userService = userService;
        this.entityLocalizationService = entityLocalizationService;
    }

    public void notifySubscription(User follower, User following) {
        String message = localize(NOTIFICATION_SUBSCRIPTION, follower);
        sportBot.sendTgMessage(following.getTelegramId(), message);
    }

    public void notifyFollowersAboutNewRecord(User user, ExerciseType exerciseType, int maxValue) {
        subscriptionService.getFollowers(user.getTelegramId())
                .forEach(follower -> {
                    Locale locale = userService.getUserLocale(follower);
                    String message = messageSource.getMessage(
                        "notification.friend.record",
                        new Object[]{user.getFullName(), entityLocalizationService.getExerciseTypeTitle(exerciseType, locale), maxValue},
                        locale
                    );
                    sportBot.sendTgMessage(follower.getTelegramId(), message);
                });
    }

    public void notifyFollowersAboutWorkout(User user, ExerciseType exerciseType, int count) {
        subscriptionService.getFollowers(user.getTelegramId())
                .forEach(follower -> {
                    Locale locale = userService.getUserLocale(follower);
                    String message = messageSource.getMessage(
                        "notification.friend.workout",
                        new Object[]{user.getFullName(), entityLocalizationService.getExerciseTypeTitle(exerciseType, locale), count},
                        locale
                    );
                    sportBot.sendTgMessage(follower.getTelegramId(), message);
                });
    }

    public String localize(String messageKey, Object user) {
        Locale locale = userService.getUserLocale((User) user);
        return messageSource.getMessage(
                messageKey,
                new Object[]{((User)user).getFullName()},
                locale
        );
    }
}
