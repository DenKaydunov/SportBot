package com.github.sportbot.controller;

import com.github.sportbot.service.AchievementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AchievementController.class)
class AchievementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AchievementService achievementService;

    @Test
    void achievementUser_shouldReturnAchievements() throws Exception {
        // Given
        Long telegramId = 123456L;
        String expectedResponse = "🏆 Достижения:\n1. Первое достижение\n2. Второе достижение";
        when(achievementService.getUserAchievement(telegramId)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/achievement")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void achievementUser_whenNoTelegramId_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/achievement"))
                .andExpect(status().isBadRequest());
    }
}
