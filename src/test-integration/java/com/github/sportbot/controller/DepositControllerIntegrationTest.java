package com.github.sportbot.controller;

import com.github.sportbot.model.User;
import com.github.sportbot.service.DepositService;
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
@WebMvcTest(DepositController.class)
class DepositControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepositService depositService;

    @MockBean
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setTelegramId(123456L);
        testUser.setBalanceTon(100);
    }

    @Test
    void depositBalance_shouldReturnSuccessMessage() throws Exception {
        // Given
        Long telegramId = 123456L;
        Integer depositValue = 10;
        String successMessage = "✅ Пополнено на 10 TON. Текущий баланс: 110 TON";
        when(depositService.depositBalance(telegramId, depositValue)).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(post("/api/v1/deposits")
                        .param("telegramId", telegramId.toString())
                        .param("depositValue", depositValue.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    @Test
    void depositBalance_whenMissingTelegramId_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/deposits")
                        .param("depositValue", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void depositBalance_whenMissingDepositValue_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/deposits")
                        .param("telegramId", "123456"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBalance_shouldReturnCurrentBalance() throws Exception {
        // Given
        Long telegramId = 123456L;
        String balanceMessage = "💰 Ваш баланс: 100 TON";
        when(userService.getUserByTelegramId(telegramId)).thenReturn(testUser);
        when(depositService.currentBalanceTon(testUser)).thenReturn(balanceMessage);

        // When & Then
        mockMvc.perform(get("/api/v1/deposits")
                        .param("telegramId", telegramId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string(balanceMessage));
    }

    @Test
    void getBalance_whenMissingTelegramId_shouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/deposits"))
                .andExpect(status().isBadRequest());
    }
}
