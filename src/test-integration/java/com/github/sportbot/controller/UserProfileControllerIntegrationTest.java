package com.github.sportbot.controller;

import com.github.sportbot.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(UserProfileController.class)
class UserProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserProfileService userProfileService;

    private final Long telegramId = 1000001L;

    @Test
    void shouldGetProfile() throws Exception {
        // Given
        String mockProfile = "👤 User Profile\n🏆 Rank: Beginner\n💪 Total: 100 reps";
        when(userProfileService.getProfile(telegramId)).thenReturn(mockProfile);

        // When & Then
        mockMvc.perform(get("/api/v1/profile")
                        .param("telegramId", String.valueOf(telegramId)))
                .andExpect(status().isOk())
                .andExpect(content().string(mockProfile));
    }
}
