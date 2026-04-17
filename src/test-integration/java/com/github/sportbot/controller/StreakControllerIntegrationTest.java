package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.service.StreakService;
import com.github.sportbot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(StreakController.class)
class StreakControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StreakService streakService;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setTelegramId(123456L);
        testUser.setCurrentStreak(5);
        testUser.setBestStreak(10);
    }

    @Test
    void getStreak_shouldReturnStreakInfo() throws Exception {
        // Given
        Long telegramId = 123456L;
        String expectedInfo = "🔥 Текущий стрик: 5 дней\n🏆 Лучший стрик: 10 дней";
        when(userService.getUserByTelegramId(telegramId)).thenReturn(testUser);
        when(streakService.getStreakInfo(testUser)).thenReturn(expectedInfo);

        // When & Then
        mockMvc.perform(get("/api/v1/streaks")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedInfo));
    }

    @Test
    void getCurrentStreak_shouldReturnCurrentStreakValue() throws Exception {
        // Given
        Long telegramId = 123456L;
        when(userService.getUserByTelegramId(telegramId)).thenReturn(testUser);
        when(streakService.getCurrentStreak(testUser)).thenReturn(5);

        // When & Then
        mockMvc.perform(get("/api/v1/streaks/current")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getBestStreak_shouldReturnBestStreakValue() throws Exception {
        // Given
        Long telegramId = 123456L;
        when(userService.getUserByTelegramId(telegramId)).thenReturn(testUser);
        when(streakService.getBestStreak(testUser)).thenReturn(10);

        // When & Then
        mockMvc.perform(get("/api/v1/streaks/best")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void saveStreak_shouldReturnSuccessMessage() throws Exception {
        // Given
        Long telegramId = 123456L;
        String expectedMessage = "✅ Стрик сохранён за 1 TON";
        when(streakService.saveStreak(telegramId)).thenReturn(expectedMessage);

        // When & Then
        mockMvc.perform(post("/api/v1/streaks/save")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedMessage));
    }

    @Test
    void getStreak_whenNoTelegramId_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/streaks"))
                .andExpect(status().isBadRequest());
    }
}
