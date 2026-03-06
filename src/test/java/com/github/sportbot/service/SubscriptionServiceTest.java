package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Subscription;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @Mock
    private ExerciseTypeService exerciseTypeService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User follower;
    private User following;

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
    }

    @Test
    void subscribe_NewSubscription_Success() {
        // Given
        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(messageSource.getMessage(eq("subscription.subscribed"), any(Object[].class), any(Locale.class)))
                .thenReturn("Вы успешно подписались на пользователя Following User.");

        // When
        String result = subscriptionService.subscribe(100L, 200L);

        // Then
        verify(subscriptionRepository).save(any(Subscription.class));
        verify(notificationService).notifySubscription(follower, following);
        assertTrue(result.contains("Following User"));
        assertEquals("Вы успешно подписались на пользователя Following User.", result);
    }

    @Test
    void subscribe_AlreadySubscribed_ReturnsAlreadySubscribedMessage() {
        // Given
        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
        when(messageSource.getMessage(eq("subscription.already_subscribed"), any(Object[].class), any(Locale.class)))
                .thenReturn("Вы уже подписаны на пользователя Following User.");

        // When
        String result = subscriptionService.subscribe(100L, 200L);

        // Then
        verify(subscriptionRepository, never()).save(any(Subscription.class));
        verify(notificationService, never()).notifySubscription(any(), any());
        assertEquals("Вы уже подписаны на пользователя Following User.", result);
    }

    @Test
    void subscribe_ToYourself_ThrowsException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                subscriptionService.subscribe(100L, 100L));

        verify(userService, never()).getUserByTelegramId(any());
        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void unsubscribe_ExistingSubscription_Success() {
        // Given
        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
        when(messageSource.getMessage(eq("subscription.unsubscribed"), any(Object[].class), any(Locale.class)))
                .thenReturn("Вы успешно отписались от пользователя Following User.");

        // When
        String result = subscriptionService.unsubscribe(100L, 200L);

        // Then
        verify(subscriptionRepository).deleteByFollowerAndFollowing(follower, following);
        assertEquals("Вы успешно отписались от пользователя Following User.", result);
    }

    @Test
    void unsubscribe_NotSubscribed_ReturnsNotSubscribedMessage() {
        // Given
        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(messageSource.getMessage(eq("subscription.not_subscribed"), any(Object[].class), any(Locale.class)))
                .thenReturn("Вы не были подписаны на пользователя Following User.");

        // When
        String result = subscriptionService.unsubscribe(100L, 200L);

        // Then
        verify(subscriptionRepository, never()).deleteByFollowerAndFollowing(any(), any());
        assertEquals("Вы не были подписаны на пользователя Following User.", result);
    }

    @Test
    void getFollowing_ReturnsListOfFollowedUsers() {
        // Given
        User user1 = User.builder().id(3).telegramId(300L).fullName("User 1").build();
        User user2 = User.builder().id(4).telegramId(400L).fullName("User 2").build();

        Subscription sub1 = Subscription.builder().follower(follower).following(user1).build();
        Subscription sub2 = Subscription.builder().follower(follower).following(user2).build();

        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(subscriptionRepository.findByFollower(follower)).thenReturn(List.of(sub1, sub2));

        // When
        List<User> result = subscriptionService.getFollowing(100L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    void getFollowers_ReturnsListOfFollowers() {
        // Given
        User follower1 = User.builder().id(3).telegramId(300L).fullName("Follower 1").build();
        User follower2 = User.builder().id(4).telegramId(400L).fullName("Follower 2").build();

        Subscription sub1 = Subscription.builder().follower(follower1).following(following).build();
        Subscription sub2 = Subscription.builder().follower(follower2).following(following).build();

        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(subscriptionRepository.findByFollowing(following)).thenReturn(List.of(sub1, sub2));

        // When
        List<User> result = subscriptionService.getFollowers(200L);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(follower1));
        assertTrue(result.contains(follower2));
    }

    @Test
    void compareProgress_UserAhead_ReturnsCorrectMessage() {
        // Given
        ExerciseType pushUp = ExerciseType.builder().id(1L).code("push_up").title("Отжимания").build();

        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(pushUp);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(follower, pushUp)).thenReturn(150);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(following, pushUp)).thenReturn(100);

        // When
        String result = subscriptionService.compareProgress(100L, 200L, "push_up");

        // Then
        assertTrue(result.contains("Отжимания"));
        assertTrue(result.contains("Ты: 150"));
        assertTrue(result.contains("Following User: 100"));
        assertTrue(result.contains("Ты впереди на 50!"));
    }

    @Test
    void compareProgress_UserBehind_ReturnsCorrectMessage() {
        // Given
        ExerciseType pullUp = ExerciseType.builder().id(2L).code("pull_up").title("Подтягивания").build();

        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(exerciseTypeService.getExerciseType("pull_up")).thenReturn(pullUp);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(follower, pullUp)).thenReturn(50);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(following, pullUp)).thenReturn(100);

        // When
        String result = subscriptionService.compareProgress(100L, 200L, "pull_up");

        // Then
        assertTrue(result.contains("Подтягивания"));
        assertTrue(result.contains("Ты: 50"));
        assertTrue(result.contains("Following User: 100"));
        assertTrue(result.contains("позади на 50"));
    }

    @Test
    void compareProgress_EqualResults_ReturnsCorrectMessage() {
        // Given
        ExerciseType squat = ExerciseType.builder().id(3L).code("squat").title("Приседания").build();

        when(userService.getUserByTelegramId(100L)).thenReturn(follower);
        when(userService.getUserByTelegramId(200L)).thenReturn(following);
        when(exerciseTypeService.getExerciseType("squat")).thenReturn(squat);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(follower, squat)).thenReturn(100);
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(following, squat)).thenReturn(100);

        // When
        String result = subscriptionService.compareProgress(100L, 200L, "squat");

        // Then
        assertTrue(result.contains("Приседания"));
        assertTrue(result.contains("Ты: 100"));
        assertTrue(result.contains("Following User: 100"));
        assertTrue(result.contains("У вас равный результат!"));
    }
}
