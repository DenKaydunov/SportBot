package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.dto.UpdateProgramRequest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;


@Disabled
@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class UserProgramControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Из test data.sql
    private final Integer telegramId = 123456789;
    private final String exerciseCode = "push_up";

    @Test
    void shouldReturnWorkoutPlan() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", telegramId.toString())
                        .param("exerciseType", exerciseCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets").isArray())
                .andExpect(jsonPath("$.totalReps").isNumber())
                .andExpect(jsonPath("$.message").isString());
    }

    @Test
    void shouldUpdateUserProgram() throws Exception {
        UpdateProgramRequest request = new UpdateProgramRequest(
                telegramId,     // Long, не int
                exerciseCode
        );

        mockMvc.perform(put("/api/v1/programs") // <-- был POST, должен быть PUT
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
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
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", telegramId.toString())
                        .param("exerciseType", exerciseCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sets[0]").value(9))   // round(17 * 0.51) = 8.67 → 9
                .andExpect(jsonPath("$.sets[1]").value(10))  // 10.37 → 10
                .andExpect(jsonPath("$.sets[2]").value(11))  // 11.22 → 11
                .andExpect(jsonPath("$.sets[3]").value(10))  // 9.52 → 10
                .andExpect(jsonPath("$.sets[4]").value(9))   // 8.67 → 9
                .andExpect(jsonPath("$.totalReps").value(49))
                .andExpect(jsonPath("$.message")
                        .value("Твоя тренировка на сегодня: 9, 10, 11, 10, 9. Всего повторений — 49."));
    }

    @Test
    @DisplayName("Should return default program when no history exists")
    @Sql(statements = {
            // 1) убрать возможную программу, если она внезапно есть
            "DELETE FROM user_programs " +
                    "WHERE user_id = (SELECT id FROM users WHERE telegram_id = 123456789) " +
                    "  AND exercise_type_id = (SELECT id FROM exercise_types WHERE code = 'squat');",

            // 2) убрать историю максимумов по squat — чтобы точно сработал дефолт
            "DELETE FROM user_max_history " +
                    "WHERE user_id = (SELECT id FROM users WHERE telegram_id = 123456789) " +
                    "  AND exercise_type_id = (SELECT id FROM exercise_types WHERE code = 'squat');"
    }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void shouldReturnDefaultProgram() throws Exception {
        mockMvc.perform(get("/api/v1/programs")
                        .param("telegramId", telegramId.toString())
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
