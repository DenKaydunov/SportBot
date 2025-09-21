package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.UpdateProgramRequest;
import com.github.sportbot.service.UserProgramService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserProgramController.class)
class UserProgramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProgramService userProgramService;

    private final Integer telegramId = 1000001;
    private final String exerciseCode = "push_up";

    @Test
    void shouldReturnWorkoutPlan() throws Exception {
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
        UpdateProgramRequest request = new UpdateProgramRequest(
                telegramId,
                exerciseCode
        );

        mockMvc.perform(put("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}