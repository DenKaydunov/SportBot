package com.github.sportbot.service;

import com.github.sportbot.exception.SubscriptionException;
import com.github.sportbot.model.Subscription;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
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
    private MessageSource messageSource;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        follower = User.builder()
                .id(1)
                .telegramId(100001L)
                .fullName("John Doe")
                .build();

        following = User.builder()
                .id(2)
                .telegramId(100002L)
                .fullName("Jane Smith")
                .build();
    }

    @Test
    void subscribe_Success_NewSubscription() {
        // Given
        Long followerId = follower.getTelegramId();
        Long followingId = following.getTelegramId();
        String followerName = follower.getFullName();

        when(userService.getOrCreateUser(followerId, followerName)).thenReturn(follower);
        when(userService.getUserByTelegramId(followingId)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(messageSource.getMessage(
                SubscriptionService.SUBSCRIBED,
                new Object[]{following.getFullName()},
                Locale.forLanguageTag("ru-RU")
        )).thenReturn("Вы подписались на Jane Smith");

        // When
        String result = subscriptionService.subscribe(followerId, followerName, followingId);

        // Then
        assertEquals("Вы подписались на Jane Smith", result);

        ArgumentCaptor<Subscription> subscriptionCaptor = ArgumentCaptor.forClass(Subscription.class);
        verify(subscriptionRepository).save(subscriptionCaptor.capture());

        Subscription savedSubscription = subscriptionCaptor.getValue();
        assertEquals(follower, savedSubscription.getFollower());
        assertEquals(following, savedSubscription.getFollowing());

        verify(notificationService).notifySubscription(follower, following);
        verify(userService).getOrCreateUser(followerId, followerName);
        verify(userService).getUserByTelegramId(followingId);
    }

    @Test
    void subscribe_AlreadySubscribed() {
        // Given
        Long followerId = follower.getTelegramId();
        Long followingId = following.getTelegramId();
        String followerName = follower.getFullName();

        when(userService.getOrCreateUser(followerId, followerName)).thenReturn(follower);
        when(userService.getUserByTelegramId(followingId)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
        when(messageSource.getMessage(
                SubscriptionService.ALREADY_SUBSCRIBED,
                new Object[]{following.getFullName()},
                Locale.forLanguageTag("ru-RU")
        )).thenReturn("Вы уже подписаны на Jane Smith");

        // When
        String result = subscriptionService.subscribe(followerId, followerName, followingId);

        // Then
        assertEquals("Вы уже подписаны на Jane Smith", result);

        verify(subscriptionRepository, never()).save(any());
        verify(notificationService, never()).notifySubscription(any(), any());
    }

    @Test
    void subscribe_SelfSubscription_ThrowsException() {
        // Given
        Long userId = follower.getTelegramId();
        String userName = follower.getFullName();

        // When & Then
        assertThrows(SubscriptionException.class, () ->
                subscriptionService.subscribe(userId, userName, userId)
        );

        verify(userService, never()).getOrCreateUser(anyLong(), anyString());
        verify(userService, never()).getUserByTelegramId(anyLong());
        verify(subscriptionRepository, never()).save(any());
        verify(notificationService, never()).notifySubscription(any(), any());
    }

    @Test
    void unsubscribe_Success() {
        // Given
        Long followerId = follower.getTelegramId();
        Long followingId = following.getTelegramId();

        when(userService.getUserByTelegramId(followerId)).thenReturn(follower);
        when(userService.getUserByTelegramId(followingId)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(true);
        when(messageSource.getMessage(
                SubscriptionService.UNSUBSCRIBED,
                new Object[]{following.getFullName()},
                Locale.forLanguageTag("ru-RU")
        )).thenReturn("Вы отписались от Jane Smith");

        // When
        String result = subscriptionService.unsubscribe(followerId, followingId);

        // Then
        assertEquals("Вы отписались от Jane Smith", result);
        verify(subscriptionRepository).deleteByFollowerAndFollowing(follower, following);
    }

    @Test
    void unsubscribe_NotSubscribed() {
        // Given
        Long followerId = follower.getTelegramId();
        Long followingId = following.getTelegramId();

        when(userService.getUserByTelegramId(followerId)).thenReturn(follower);
        when(userService.getUserByTelegramId(followingId)).thenReturn(following);
        when(subscriptionRepository.existsByFollowerAndFollowing(follower, following)).thenReturn(false);
        when(messageSource.getMessage(
                SubscriptionService.NOT_SUBSCRIBED,
                new Object[]{following.getFullName()},
                Locale.forLanguageTag("ru-RU")
        )).thenReturn("Вы не подписаны на Jane Smith");

        // When
        String result = subscriptionService.unsubscribe(followerId, followingId);

        // Then
        assertEquals("Вы не подписаны на Jane Smith", result);
        verify(subscriptionRepository, never()).deleteByFollowerAndFollowing(any(), any());
    }

    @Test
    void getFollowing_Success() {
        // Given
        Long telegramId = follower.getTelegramId();
        User user1 = User.builder().id(10).telegramId(200001L).fullName("User 1").build();
        User user2 = User.builder().id(11).telegramId(200002L).fullName("User 2").build();

        Subscription sub1 = Subscription.builder().follower(follower).following(user1).build();
        Subscription sub2 = Subscription.builder().follower(follower).following(user2).build();

        when(userService.getUserByTelegramId(telegramId)).thenReturn(follower);
        when(subscriptionRepository.findByFollower(follower)).thenReturn(Arrays.asList(sub1, sub2));

        // When
        List<User> result = subscriptionService.getFollowing(telegramId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(subscriptionRepository).findByFollower(follower);
    }

    @Test
    void getFollowers_Success() {
        // Given
        Long telegramId = following.getTelegramId();
        User user1 = User.builder().id(10).telegramId(200001L).fullName("User 1").build();
        User user2 = User.builder().id(11).telegramId(200002L).fullName("User 2").build();

        Subscription sub1 = Subscription.builder().follower(user1).following(following).build();
        Subscription sub2 = Subscription.builder().follower(user2).following(following).build();

        when(userService.getUserByTelegramId(telegramId)).thenReturn(following);
        when(subscriptionRepository.findByFollowing(following)).thenReturn(Arrays.asList(sub1, sub2));

        // When
        List<User> result = subscriptionService.getFollowers(telegramId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
        verify(subscriptionRepository).findByFollowing(following);
    }
}
