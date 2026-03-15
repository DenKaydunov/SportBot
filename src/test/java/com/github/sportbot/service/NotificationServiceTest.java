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
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private SportBot sportBot;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private MessageSource messageSource;

    @Mock
    UserService userService;

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
                .language("ru")
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

        // Setup MessageSource mock (lenient because not all tests use it)
        lenient().when(messageSource.getMessage(
                eq(NotificationService.NOTIFICATION_SUBSCRIPTION),
                any(Object[].class),
                any(Locale.class)
        )).thenAnswer(invocation -> "🔥 На вас подписался пользователь: Follower User");
    }

    @Test
    void notifySubscription_SendsMessageToFollowing() {
        // When
        when(userService.getUserLocale(any())).thenReturn(Locale.forLanguageTag("ru"));
        notificationService.notifySubscription(follower, following);

        // Then
        verify(sportBot).sendTgMessage(eq(200L), contains("Follower User"));
    }

    @Test
    void notifyFollowersAboutWorkout_SendsMessagesToAllFollowers() {
        // Given
        User follower1 = User.builder().id(3).telegramId(300L).fullName("Follower 1").build();
        User follower2 = User.builder().id(4).telegramId(400L).fullName("Follower 2").build();

        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of(follower1, follower2));

        // When
        notificationService.notifyFollowersAboutWorkout(following, exerciseType, 50);

        // Then
        verify(sportBot, times(2)).sendTgMessage(anyLong(), (String) any());
        verify(sportBot).sendTgMessage(eq(300L), argThat((String message) ->
                message.contains("Following User") &&
                message.contains("Отжимания") &&
                message.contains("50")
        ));
        verify(sportBot).sendTgMessage(eq(400L), argThat((String message) ->
                message.contains("Following User") &&
                message.contains("Отжимания") &&
                message.contains("50")
        ));
    }

    @Test
    void notifyFollowersAboutNewRecord_SendsMessagesToAllFollowers() {
        // Given
        User follower1 = User.builder().id(3).telegramId(300L).fullName("Follower 1").build();

        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of(follower1));

        // When
        notificationService.notifyFollowersAboutNewRecord(following, exerciseType, 100);

        // Then
        verify(sportBot).sendTgMessage(eq(300L), argThat((String message) ->
                message.contains("Following User") &&
                message.contains("побил личный рекорд") &&
                message.contains("Отжимания") &&
                message.contains("100")
        ));
    }

    @Test
    void notifyFollowersAboutWorkout_NoFollowers_DoesNotSendMessages() {
        // Given
        when(subscriptionService.getFollowers(following.getTelegramId()))
                .thenReturn(List.of());

        // When
        notificationService.notifyFollowersAboutWorkout(following, exerciseType, 50);

        // Then
        verify(sportBot, never()).sendTgMessage(anyLong(), (String) any());
    }
}
