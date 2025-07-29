package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.UpdateProgramRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserProgramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    //From data.sql
    private final Integer telegramId = 123456789;
    private final String exerciseCode = "push_up";

    @Test
    void shouldReturnWorkoutPlan() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", telegramId.toString())
                        .param("exerciseType", exerciseCode))
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

        mockMvc.perform(post("/api/v1/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
    }

    @Test
    void shouldCreateProgramAfterPostingMaxAndReturnCorrectWorkoutSets() throws Exception {
        mockMvc.perform(post("/api/v1/exercises/max")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "telegramId": 123456789,
                    "exerciseType": "push_up",
                    "count": 17
                }
            """));

        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", "123456789")
                        .param("exerciseType", "push_up"))
                .andExpect(jsonPath("$.sets[0]").value(9))   // round(17 * 0.51) = 8.67 → 9
                .andExpect(jsonPath("$.sets[1]").value(10))  // 10.37 → 10
                .andExpect(jsonPath("$.sets[2]").value(11))  // 11.22 → 11
                .andExpect(jsonPath("$.sets[3]").value(10))  // 9.52 → 10
                .andExpect(jsonPath("$.sets[4]").value(9))   // 8.67 → 9
                .andExpect(jsonPath("$.totalReps").value(49))
                .andExpect(jsonPath("$.message").value("Твоя тренировка на сегодня: 9, 10, 11, 10, 9. Всего повторений — 49."));

    }

    @Test
    @DisplayName("Should return default program when no history exists")
    void shouldReturnDefaultProgram() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", "123456789")
                        .param("exerciseType", "squat")
                        .header("Accept-Language", "ru"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets.length()").value(5))
                .andExpect(jsonPath("$.sets[0]").value(3))
                .andExpect(jsonPath("$.sets[1]").value(3))
                .andExpect(jsonPath("$.sets[2]").value(3))
                .andExpect(jsonPath("$.sets[3]").value(3))
                .andExpect(jsonPath("$.sets[4]").value(3))
                .andExpect(jsonPath("$.totalReps").value(15))
                .andExpect(jsonPath("$.message")
                        .value("Твоя тренировка на сегодня: 3, 3, 3, 3, 3. Всего повторений — 15."));

    }
}
