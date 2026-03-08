package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementAggregationScheduler {

    private final AchievementAggregationService achievementService;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 12 1 * *")
    public void sendAchievementForMonth(){
        String message = achievementService.getAchievementForMonth();

        List<User> subscribeUser = userRepository.findAllByIsSubscribedTrue();
        for (User user : subscribeUser){
            if (!message.isEmpty()){
                notificationService.sendTelegramMessage(user.getTelegramId(), message);
            }
        }
    }
}
