package com.github.sportbot.controller;

import com.github.sportbot.service.CompetitorsService;
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
@WebMvcTest(CompetitorsController.class)
class CompetitorsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CompetitorsService competitorsService;

    @Test
    void getCompetitorsAllTime_shouldReturnCompetitorsList() throws Exception {
        // Given
        Long telegramId = 123456L;
        String exerciseCode = "pushups";
        String expectedResponse = "🏆 Конкуренты\nУпражнение: Отжимания\nЗа всё время\n\n👉 1. Иван — 500\n2. Петр — 450";

        when(competitorsService.getCompetitorsAllTime(exerciseCode, telegramId))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/competitors/{exerciseCode}", exerciseCode)
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void getCompetitorsAllTime_whenNoTelegramId_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/competitors/{exerciseCode}", "pushups"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCompetitorsAllTime_withDifferentExercises_shouldWork() throws Exception {
        // Given
        Long telegramId = 123456L;
        String squatsResponse = "🏆 Конкуренты\nУпражнение: Приседания\n";

        when(competitorsService.getCompetitorsAllTime("squats", telegramId))
                .thenReturn(squatsResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/competitors/{exerciseCode}", "squats")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(squatsResponse));
    }
}
