package com.github.sportbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementAggregationScheduler {

    private final AchievementAggregationService achievementService;

    @Scheduled(cron = "0 0 12 1 * *")
    public void sendAchievementForMonth() {
        log.info("Scheduled achievement sending triggered");
        achievementService.sendAchievementCongratulation();
    }
}
