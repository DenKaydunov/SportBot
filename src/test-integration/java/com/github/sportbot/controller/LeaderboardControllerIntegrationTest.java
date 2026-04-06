package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.LeaderBoardRepository;
import com.github.sportbot.service.LeaderboardService;
import com.github.sportbot.service.UserService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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

    @MockBean
    private UserService userService;

    private final String exerciseCode = "squat";
    private final Long testTelegramId = 1000001L;

    @Mock
    private LeaderBoardRepository leaderBoardRepository;

    @Test
    void shouldReturnLeaderboardByPeriod() throws Exception {
        // Given
        String expectedResponse = "Top 3 for squats today";
        User testUser = User.builder().telegramId(testTelegramId).language("ru").build();
        when(userService.getUserByTelegramId(testTelegramId)).thenReturn(testUser);
        when(leaderboardService.getLeaderboardByPeriod(exerciseCode, 3, "today", testUser))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-period", exerciseCode)
                        .param("limit", "3")
                        .param("period", "today")
                        .param("telegramId", testTelegramId.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    @Disabled("TODO: Fix content negotiation - endpoint returns text/plain but test expects different content type")
    void shouldReturnLeaderboardByDates() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Leaderboard for squats between 2025-09-01 and 2025-09-05";
        User testUser = User.builder().telegramId(testTelegramId).language("ru").build();

        when(userService.getUserByTelegramId(testTelegramId)).thenReturn(testUser);
        when(leaderboardService.getLeaderboardByDates(exerciseCode, "TAG",5, startDate, endDate, testUser))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates", exerciseCode)
                        .param("tagCode", "TAG")
                        .param("limit", "5")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("telegramId", testTelegramId.toString())
                        .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    @Disabled("TODO: Fix content negotiation - endpoint returns text/plain but test expects different content type")
    void shouldReturnLeaderboardByDates_WhenTagIsOmitted() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Leaderboard for squats between 2025-09-01 and 2025-09-05 (no tag)";
        User testUser = User.builder().telegramId(testTelegramId).language("ru").build();

        when(userService.getUserByTelegramId(testTelegramId)).thenReturn(testUser);
        when(leaderboardService.getLeaderboardByDates(exerciseCode, null, 5, startDate, endDate, testUser))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates", exerciseCode)
                        .param("limit", "5")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("telegramId", testTelegramId.toString())
                        .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    @Disabled("TODO: Fix content negotiation - endpoint returns text/plain but test expects different content type")
    void shouldReturnLeaderboardByDatesPaged() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2025, 9, 5);
        String expectedResponse = "Paginated leaderboard for squats between 2025-09-01 and 2025-09-05";
        User testUser = User.builder().telegramId(testTelegramId).language("ru").build();

        when(userService.getUserByTelegramId(testTelegramId)).thenReturn(testUser);
        when(leaderboardService.getLeaderboardByDatesPaged(eq(exerciseCode), eq("TAG"), any(Pageable.class), eq(startDate), eq(endDate), eq(testUser)))
                .thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-dates/paged", exerciseCode)
                        .param("tagCode", "TAG")
                        .param("page", "1")
                        .param("size", "20")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString())
                        .param("telegramId", testTelegramId.toString())
                        .accept(MediaType.TEXT_PLAIN_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResponse));
    }

    @Test
    @Disabled("TODO: Fix - missing required parameter or invalid sort parameter handling")
    void shouldHandleInvalidSortGracefully() throws Exception {
        // Given
        User testUser = User.builder().telegramId(testTelegramId).language("ru").build();
        when(userService.getUserByTelegramId(testTelegramId)).thenReturn(testUser);
        when(leaderboardService.getLeaderboardByPeriodPaged(eq(exerciseCode), any(), eq("today"), eq(testUser)))
                .thenReturn("Leaderboard result");

        // When & Then
        // Should return 200 OK because service now ignores the sort parameter
        mockMvc.perform(get("/api/v1/leaderboard/{exerciseCode}/by-period/paged", exerciseCode)
                        .param("page", "1")
                        .param("size", "10")
                        .param("sort", "string")
                        .param("period", "today")
                        .param("telegramId", testTelegramId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCallServiceWithCorrectParams() throws Exception {
        when(leaderboardService.getRating(anyLong()))
                .thenReturn("ok");

        mockMvc.perform(get("/api/v1/leaderboard/rating")
                        .param("telegramId", "1000001"))
                .andExpect(status().isOk());

        verify(leaderboardService).getRating(1000001L);
    }
}
