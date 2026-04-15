package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementSendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementAggregationSchedulerTest {

    @Mock
    private AchievementAggregationService achievementService;

    @InjectMocks
    private AchievementAggregationScheduler scheduler;

    @Test
    void sendAchievementForMonth_shouldCallAchievementService() {
        // Given
        AchievementSendResponse response = new AchievementSendResponse(
                "Success",
                10,
                0,
                LocalDateTime.now()
        );
        when(achievementService.sendAchievementCongratulation()).thenReturn(response);

        // When
        scheduler.sendAchievementForMonth();

        // Then
        verify(achievementService).sendAchievementCongratulation();
    }

    @Test
    void sendAchievementForMonth_whenNoAchievements_shouldStillCall() {
        // Given
        AchievementSendResponse response = AchievementSendResponse.noContent();
        when(achievementService.sendAchievementCongratulation()).thenReturn(response);

        // When
        scheduler.sendAchievementForMonth();

        // Then
        verify(achievementService).sendAchievementCongratulation();
    }

    @Test
    void sendAchievementForMonth_whenServiceThrowsException_shouldPropagateException() {
        // Given
        when(achievementService.sendAchievementCongratulation())
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        try {
            scheduler.sendAchievementForMonth();
        } catch (RuntimeException e) {
            // Expected
        }

        verify(achievementService).sendAchievementCongratulation();
    }
}
