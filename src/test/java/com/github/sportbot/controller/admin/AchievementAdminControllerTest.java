package com.github.sportbot.controller.admin;

import com.github.sportbot.dto.admin.AchievementLocalizationDto;
import com.github.sportbot.service.AchievementAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AchievementAdminController.class)
class AchievementAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AchievementAdminService achievementAdminService;

    @Test
    void updateLocalization_withValidLanguageCode_shouldReturn200() throws Exception {
        AchievementLocalizationDto dto = new AchievementLocalizationDto();
        dto.setLanguage("ru");
        dto.setTitle("Test Title");
        dto.setDescription("Test Description");

        when(achievementAdminService.updateLocalization(anyLong(), anyString(), anyString(), anyString()))
                .thenReturn(dto);

        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "ru")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isOk());
    }

    @Test
    void updateLocalization_withInvalidLanguageCode_shouldReturn400() throws Exception {
        // Invalid: 3 letters
        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "rus")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isBadRequest());

        // Invalid: 1 letter
        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "r")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isBadRequest());

        // Invalid: uppercase
        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "RU")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isBadRequest());

        // Invalid: mixed case
        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "Ru")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isBadRequest());

        // Invalid: special characters
        mockMvc.perform(put("/admin/achievements/1/localization")
                        .param("language", "r-")
                        .param("title", "Test Title")
                        .param("description", "Test Description"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteLocalization_withValidLanguageCode_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/admin/achievements/1/localization/ru"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLocalization_withInvalidLanguageCode_shouldReturn400() throws Exception {
        // Invalid: 3 letters
        mockMvc.perform(delete("/admin/achievements/1/localization/rus"))
                .andExpect(status().isBadRequest());

        // Invalid: uppercase
        mockMvc.perform(delete("/admin/achievements/1/localization/RU"))
                .andExpect(status().isBadRequest());

        // Invalid: special characters
        mockMvc.perform(delete("/admin/achievements/1/localization/r-"))
                .andExpect(status().isBadRequest());
    }
}
