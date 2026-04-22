package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.UpdateProgramRequest;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.service.UserProgramService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
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

    private final Long telegramId = 1000001L;
    private final String exerciseCode = "push_up";

    @Test
    void shouldReturnWorkoutPlan() throws Exception {
        // Given
        WorkoutPlanResponse mockResponse = new WorkoutPlanResponse(
                List.of(10, 10, 8, 8, 6),
                42,
                "Your workout today: 10, 10, 8, 8, 6 (Total: 42 reps)"
        );
        when(userProgramService.getWorkoutPlan(telegramId, exerciseCode))
                .thenReturn(mockResponse);

        // When & Then
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
        doNothing().when(userProgramService).incrementDayProgram(telegramId, exerciseCode);

        // When & Then
        mockMvc.perform(put("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userProgramService, times(1)).incrementDayProgram(telegramId, exerciseCode);
    }
}
