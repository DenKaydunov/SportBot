package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@Slf4j
public class NotificationService {

    private final SportBot sportBot;
    private final SubscriptionService subscriptionService;

    public NotificationService(@Lazy SportBot sportBot, @Lazy SubscriptionService subscriptionService) {
        this.sportBot = sportBot;
        this.subscriptionService = subscriptionService;
    }

    public void notifySubscription(User follower, User following) {
        String text = String.format("На тебя подписался пользователь %s!", follower.getFullName());
        sendTelegramMessage(following.getTelegramId(), text);
    }

    public void notifyFollowersAboutWorkout(User user, ExerciseType exerciseType, int count) {
        String achievement = String.format("выполнил тренировку: %s (%d)", exerciseType.getTitle(), count);
        notifyFollowers(user, achievement);
    }

    public void notifyFollowersAboutNewRecord(User user, ExerciseType exerciseType, int maxValue) {
        String achievement = String.format("побил личный рекорд в %s: %d!", exerciseType.getTitle(), maxValue);
        notifyFollowers(user, achievement);
    }

    private void notifyFollowers(User user, String achievement) {
        subscriptionService.getFollowers(user.getTelegramId()).forEach(follower ->
                sendTelegramMessage(follower.getTelegramId(),
                        String.format("Твой друг %s %s", user.getFullName(), achievement)));
    }

    public void sendTelegramMessage(Long telegramId, String text) {
        SendMessage message = new SendMessage(telegramId.toString(), text);
        try {
            sportBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send telegram message to {}: {}", telegramId, e.getMessage());
        }
    }
}
