package com.github.sportbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeeklyLeaderboardScheduler {

    private final WeeklyLeaderboardService weeklyLeaderboardService;

    @Scheduled(cron = "0 0 11 * * MON")
    public void sendWeeklyLeaderboard() {
        log.info("Weekly leaderboard scheduled task triggered");
        weeklyLeaderboardService.sendWeeklyCongratulations();
    }
}
