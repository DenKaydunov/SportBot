package com.github.sportbot.controller;

import com.github.sportbot.repository.LeaderBoardRepository;
import com.github.sportbot.service.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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

    @Mock
    private LeaderBoardRepository leaderBoardRepository;

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

    @Test
    void shouldReturnLeaderboardByDatesPaged() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Paginated leaderboard for squats between 2025-09-01 and 2025-09-05";

        when(leaderboardService.getLeaderboardByDatesPaged(eq(exerciseCode), eq("TAG"), any(Pageable.class), eq(startDate), eq(endDate)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates/paged", exerciseCode)
                        .param("tagCode", "TAG")
                        .param("page", "1")
                        .param("size", "20")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    void shouldHandleInvalidSortGracefully() throws Exception {
        // Given
        // When & Then
        // Should return 200 OK because service now ignores the sort parameter
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-period/paged", exerciseCode)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "string")
                        .param("period", "today")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCallServiceWithCorrectParams() throws Exception {
        when(leaderboardService.getTopAllExercises(anyLong(), anyInt()))
                .thenReturn("ok");

        mockMvc.perform(get("/api/v1/leaderboard/top")
                        .param("userId", "4")
                        .param("limit", "10"))
                .andExpect(status().isOk());

        verify(leaderboardService).getTopAllExercises(4L, 10);
    }
}
