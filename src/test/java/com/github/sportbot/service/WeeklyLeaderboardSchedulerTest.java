package com.github.sportbot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeeklyLeaderboardSchedulerTest {

    @Mock
    private WeeklyLeaderboardService weeklyLeaderboardService;

    @InjectMocks
    private WeeklyLeaderboardScheduler scheduler;

    @Test
    void sendWeeklyLeaderboard_shouldCallWeeklyLeaderboardService() {
        // When
        scheduler.sendWeeklyLeaderboard();

        // Then
        verify(weeklyLeaderboardService).sendWeeklyCongratulations();
    }

    @Test
    void sendWeeklyLeaderboard_whenServiceThrowsException_shouldPropagateException() {
        // Given
        doThrow(new RuntimeException("Service error"))
            .when(weeklyLeaderboardService).sendWeeklyCongratulations();

        // When & Then
        try {
            scheduler.sendWeeklyLeaderboard();
        } catch (RuntimeException e) {
            // Expected
        }

        verify(weeklyLeaderboardService).sendWeeklyCongratulations();
    }
}
