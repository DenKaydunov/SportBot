package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UpdateLanguageRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.model.Sex;
import com.github.sportbot.model.User;
import com.github.sportbot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User testUser;
    private RegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setTelegramId(123456L);
        testUser.setFullName("John Doe");
        testUser.setLanguage("ru");

        registrationRequest = new RegistrationRequest(
                123456L,
                "sendpulse-123",
                true,
                "John Doe",
                "ru",
                Sex.MAN,
                25,
                null,
                null
        );
    }

    @Test
    void registerUser_shouldReturnUserResponse() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(
                "User registered successfully",
                123456L,
                "John Doe"
        );
        when(userService.registerUser(any(RegistrationRequest.class))).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(userResponse)));
    }

    @Test
    void getUserLocale_shouldReturnLanguageCode() throws Exception {
        // Given
        Long telegramId = 123456L;
        when(userService.getUserByTelegramId(telegramId)).thenReturn(testUser);
        when(userService.getUserLocale(testUser)).thenReturn(new Locale("ru"));

        // When & Then
        mockMvc.perform(get("/api/v1/users/{telegramId}/locale", telegramId))
                .andExpect(status().isOk())
                .andExpect(content().string("ru"));
    }

    @Test
    void updateUserLanguage_shouldReturnSuccessMessage() throws Exception {
        // Given
        Long telegramId = 123456L;
        UpdateLanguageRequest request = new UpdateLanguageRequest("en");
        String successMessage = "Language updated to: en";
        when(userService.updateUserLanguage(telegramId, request)).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{telegramId}/locale", telegramId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }

    @Test
    void updateUserLanguage_withInvalidLanguage_shouldReturnBadRequest() throws Exception {
        // Given
        Long telegramId = 123456L;
        UpdateLanguageRequest request = new UpdateLanguageRequest("fr"); // Invalid language

        // When & Then
        mockMvc.perform(patch("/api/v1/users/{telegramId}/locale", telegramId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unsubscribeUser_shouldReturnSuccessMessage() throws Exception {
        // Given
        Long telegramId = 123456L;
        String successMessage = "User unsubscribed successfully";
        when(userService.unsubscribeUser(telegramId)).thenReturn(successMessage);

        // When & Then
        mockMvc.perform(post("/api/v1/users/unsubscribe/{telegramId}", telegramId))
                .andExpect(status().isOk())
                .andExpect(content().string(successMessage));
    }
}
