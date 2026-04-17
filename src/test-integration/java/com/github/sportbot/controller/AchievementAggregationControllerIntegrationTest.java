package com.github.sportbot.controller;

import com.github.sportbot.dto.AchievementSendResponse;
import com.github.sportbot.service.AchievementAggregationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AchievementAggregationController.class)
class AchievementAggregationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AchievementAggregationService aggregationService;

    @Test
    void getAchievementForMonth_shouldReturnAchievementsList() throws Exception {
        // Given
        String expectedResponse = "🏆 Достижения за месяц:\n1. User1 - 100 отжиманий\n2. User2 - 10 дней стрик";
        when(aggregationService.getAchievementForMonth()).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/achievements/congratulation"))
                .andExpect(status().isOk());
    }

    @Test
    void sendAchievementCongratulations_shouldReturnSuccessResponse() throws Exception {
        // Given
        AchievementSendResponse response = new AchievementSendResponse(
                "Achievement congratulations sent successfully",
                50,
                2,
                LocalDateTime.now()
        );
        when(aggregationService.sendAchievementCongratulation()).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/achievements/congratulation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Achievement congratulations sent successfully"))
                .andExpect(jsonPath("$.totalUsersMessaged").value(50))
                .andExpect(jsonPath("$.failedMessages").value(2));
    }

    @Test
    void sendAchievementCongratulations_whenNoAchievements_shouldReturnNoContentResponse() throws Exception {
        // Given
        AchievementSendResponse response = AchievementSendResponse.noContent();
        when(aggregationService.sendAchievementCongratulation()).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/achievements/congratulation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No achievements to send - message was empty"))
                .andExpect(jsonPath("$.totalUsersMessaged").value(0))
                .andExpect(jsonPath("$.failedMessages").value(0));
    }
}
