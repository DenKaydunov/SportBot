package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SportBot sportBot;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private NotificationService notificationService;

    private User follower;
    private User following;
    private ExerciseType exerciseType;

    @BeforeEach
    void setUp() {
        follower = User.builder()
                .id(1)
                .telegramId(100L)
                .fullName("Follower User")
                .build();

        following = User.builder()
                .id(2)
                .telegramId(200L)
                .fullName("Following User")
                .build();

        exerciseType = ExerciseType.builder()
                .id(1L)
                .code("push_up")
                .title("Отжимания")
                .build();
    }

    @Test
    void notifySubscription_SendsMessageToFollowing() throws TelegramApiException {
        // When
        notificationService.notifySubscription(follower, following);

        // Then
        verify(sportBot).execute(argThat((SendMessage message) ->
                message.getChatId().equals("200") &&
                message.getText().contains("Follower User")
        ));
    }

    @Test
    void notifyFollowersAboutWorkout_SendsMessagesToAllFollowers() throws TelegramApiException {
        // Given
        User follower1 = User.builder().id(3).telegramId(300L).fullName("Follower 1").build();
        User follower2 = User.builder().id(4).telegramId(400L).fullName("Follower 2").build();

        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of(follower1, follower2));

        // When
        notificationService.notifyFollowersAboutWorkout(following, exerciseType, 50);

        // Then
        verify(sportBot, times(2)).execute(any(SendMessage.class));
        verify(sportBot).execute(argThat((SendMessage message) ->
                message.getChatId().equals("300") &&
                message.getText().contains("Following User") &&
                message.getText().contains("Отжимания") &&
                message.getText().contains("50")
        ));
        verify(sportBot).execute(argThat((SendMessage message) ->
                message.getChatId().equals("400") &&
                message.getText().contains("Following User") &&
                message.getText().contains("Отжимания") &&
                message.getText().contains("50")
        ));
    }

    @Test
    void notifyFollowersAboutNewRecord_SendsMessagesToAllFollowers() throws TelegramApiException {
        // Given
        User follower1 = User.builder().id(3).telegramId(300L).fullName("Follower 1").build();

        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of(follower1));

        // When
        notificationService.notifyFollowersAboutNewRecord(following, exerciseType, 100);

        // Then
        verify(sportBot).execute(argThat((SendMessage message) ->
                message.getChatId().equals("300") &&
                message.getText().contains("Following User") &&
                message.getText().contains("побил личный рекорд") &&
                message.getText().contains("Отжимания") &&
                message.getText().contains("100")
        ));
    }

    @Test
    void sendTelegramMessage_Success() throws TelegramApiException {
        // When
        notificationService.sendTelegramMessage(100L, "Test message");

        // Then
        verify(sportBot).execute(argThat((SendMessage message) ->
                message.getChatId().equals("100") &&
                message.getText().equals("Test message")
        ));
    }

    @Test
    void sendTelegramMessage_TelegramApiException_LogsError() throws TelegramApiException {
        // Given
        doThrow(new TelegramApiException("API Error"))
                .when(sportBot).execute(any(SendMessage.class));

        // When
        notificationService.sendTelegramMessage(100L, "Test message");

        // Then
        verify(sportBot).execute(any(SendMessage.class));
        // Verify that no exception is thrown (error is logged instead)
    }

    @Test
    void notifyFollowersAboutWorkout_NoFollowers_DoesNotSendMessages() throws TelegramApiException {
        // Given
        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of());

        // When
        notificationService.notifyFollowersAboutWorkout(following, exerciseType, 50);

        // Then
        verify(sportBot, never()).execute(any(SendMessage.class));
    }
}
