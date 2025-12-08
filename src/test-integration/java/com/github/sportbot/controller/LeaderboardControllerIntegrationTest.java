package com.github.sportbot.controller;

import com.github.sportbot.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    private final String exerciseCode = "squat";

    @Test
    void shouldReturnLeaderboardByPeriod() throws Exception {
        // Given
        String expectedResponse = "Top 3 for squats today";
        when(leaderboardService.getLeaderboardByPeriod(exerciseCode, 3, "today"))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-period", exerciseCode)
                        .param("limit", "3")
                        .param("period", "today")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void shouldReturnLeaderboardByDates() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Leaderboard for squats between 2025-09-01 and 2025-09-05";

        when(leaderboardService.getLeaderboardByDates(exerciseCode, "TAG",5, startDate, endDate))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates", exerciseCode)
                        .param("tagCode", "TAG")
                        .param("limit", "5")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void shouldReturnLeaderboardByDates_WhenTagIsOmitted() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Leaderboard for squats between 2025-09-01 and 2025-09-05 (no tag)";

        when(leaderboardService.getLeaderboardByDates(exerciseCode, null, 5, startDate, endDate))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates", exerciseCode)
                        .param("limit", "5")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }
}
