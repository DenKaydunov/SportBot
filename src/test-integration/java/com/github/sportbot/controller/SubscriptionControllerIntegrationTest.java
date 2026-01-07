package com.github.sportbot.controller;

import com.github.sportbot.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(SubscriptionController.class)
class SubscriptionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionService subscriptionService;


    @Test
    void shouldSubscribe() throws Exception {
        when(subscriptionService.subscribe(1L, 2L)).thenReturn("Success subscribe");
        mockMvc.perform(post("/api/v1/subscriptions/1/subscribe/2"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Success subscribe"));
        verify(subscriptionService).subscribe(1L, 2L);
    }

    @Test
    void shouldUnsubscribe() throws Exception {
        when(subscriptionService.unsubscribe(1L, 2L)).thenReturn("Success unsubscribe");
        mockMvc.perform(post("/api/v1/subscriptions/1/unsubscribe/2"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content().string("Success unsubscribe"));
        verify(subscriptionService).unsubscribe(1L, 2L);
    }
}
