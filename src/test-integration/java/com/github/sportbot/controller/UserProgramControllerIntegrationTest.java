package com.github.sportbot.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.UpdateProgramRequest;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserProgramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final Integer telegramId = 1000001;
    private final String exerciseCode = "push_up";

    @Test
    void shouldReturnWorkoutPlan() throws Exception {
        // Then
        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", String.valueOf(telegramId))
                        .param("exerciseType", exerciseCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").isArray())
                .andExpect(jsonPath("$.totalReps").isNumber())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void shouldUpdateUserProgram() throws Exception {
        // Given
        UpdateProgramRequest request = new UpdateProgramRequest(
                telegramId,
                exerciseCode
        );

        // Then
        mockMvc.perform(put("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
