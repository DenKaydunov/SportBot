package com.github.sportbot.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sportbot.service.MotivationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(MotivationController.class)
public class MotivationControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MotivationService  motivationService;

    private static final String exerciseType = "squat";

    @Test
    void shouldSaveExerciseEntry() throws Exception {
        mockMvc.perform(get("/api/v1/motivation")
                        .param("exerciseType", exerciseType)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getMotivation_WithEnglishLocale_ReturnsEnglishMessage() throws Exception {
        // given
        when(motivationService.getMotivation("squat", "en"))
                .thenReturn("No pain, no gain!");

        // when & then
        mockMvc.perform(get("/api/v1/motivation")
                        .param("exerciseType", "squat")
                        .param("locale", "en"))
                .andExpect(status().isOk())
                .andExpect(content().string("No pain, no gain!"));
    }

    @Test
    void getMotivation_WithRussianLocale_ReturnsRussianMessage() throws Exception {
        // given
        when(motivationService.getMotivation("squat", "ru"))
                .thenReturn("Давай, ещё немного!");

        // when & then
        mockMvc.perform(get("/api/v1/motivation")
                        .param("exerciseType", "squat")
                        .param("locale", "ru"))
                .andExpect(status().isOk())
                .andExpect(content().string("Давай, ещё немного!"));
    }

    @Test
    void getMotivation_WithoutLocale_DefaultsToRussian() throws Exception {
        // given
        when(motivationService.getMotivation("squat", "ru"))
                .thenReturn("Давай, ещё немного!");

        // when & then
        mockMvc.perform(get("/api/v1/motivation")
                        .param("exerciseType", "squat"))
                .andExpect(status().isOk())
                .andExpect(content().string("Давай, ещё немного!"));
    }
}
