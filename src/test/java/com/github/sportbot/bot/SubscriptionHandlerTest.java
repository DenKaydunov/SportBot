package com.github.sportbot.bot;

import com.github.sportbot.model.User;
import com.github.sportbot.service.SubscriptionService;
import com.github.sportbot.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.MessageSource;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SubscriptionHandlerTest {

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private UserService userService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private SportBot sportBot;

    @InjectMocks
    private SubscriptionHandler subscriptionHandler;

    private Update update;
    private CallbackQuery callbackQuery;
    private org.telegram.telegrambots.meta.api.objects.User telegramUser;
    private User dbUser;

    @BeforeEach
    void setUp() {
        update = mock(Update.class);
        callbackQuery = mock(CallbackQuery.class);
        telegramUser = mock(org.telegram.telegrambots.meta.api.objects.User.class);

        dbUser = User.builder()
                .id(2)
                .telegramId(100002L)
                .fullName("Jane Smith")
                .build();

        when(update.getCallbackQuery()).thenReturn(callbackQuery);
        when(callbackQuery.getFrom()).thenReturn(telegramUser);

        // Setup MessageSource mocks
        setupMessageSourceMocks();
    }

    private void setupMessageSourceMocks() {
        Locale locale = Locale.forLanguageTag("ru");

        // Сообщения без параметров
        lenient().when(messageSource.getMessage(eq("subscription.callback.success"), isNull(), any(Locale.class)))
                .thenReturn("Вы успешно подписались!");
        lenient().when(messageSource.getMessage(eq("unsubscription.callback.success"), isNull(), any(Locale.class)))
                .thenReturn("Вы успешно отписались!");
        lenient().when(messageSource.getMessage(eq("error.user.not.found"), isNull(), any(Locale.class)))
                .thenReturn("Ошибка: пользователь не найден");
        lenient().when(messageSource.getMessage(eq("error.invalid.data"), isNull(), any(Locale.class)))
                .thenReturn("Ошибка: некорректные данные");
        lenient().when(messageSource.getMessage(eq("error.subscription.general"), isNull(), any(Locale.class)))
                .thenReturn("Произошла ошибка при подписке");
        lenient().when(messageSource.getMessage(eq("error.unsubscription.general"), isNull(), any(Locale.class)))
                .thenReturn("Произошла ошибка при отписке");

        // Сообщения с параметрами
        lenient().when(messageSource.getMessage(eq("subscription.message.success"), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "✅ Вы подписались на пользователя: " + args[0];
                });
        lenient().when(messageSource.getMessage(eq("subscription.notification"), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "🔥 На вас подписался пользователь: " + args[0];
                });
        lenient().when(messageSource.getMessage(eq("unsubscription.message.success"), any(Object[].class), any(Locale.class)))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return "❌ Вы отписались от пользователя: " + args[0];
                });
    }

    @Test
    void handleSubscription_Success_WithFullName() {
        // Given
        Long followerId = 100001L;
        Long followingId = 100002L;
        String callbackData = "sub_100002";
        String callbackQueryId = "callback123";

        User currentUser = User.builder()
                .id(1)
                .telegramId(followerId)
                .fullName("John Doe")
                .language("ru")
                .build();

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(callbackQueryId);
        when(telegramUser.getId()).thenReturn(followerId);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(userService.getUserByTelegramId(followerId)).thenReturn(currentUser);
        when(userService.getUserByTelegramId(followingId)).thenReturn(dbUser);
        when(userService.getUserLocale(currentUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(userService.getUserLocale(dbUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(subscriptionService.subscribe(followerId, "John Doe", followingId))
                .thenReturn("Вы подписались на Jane Smith");

        // When
        subscriptionHandler.handleSubscription(update, sportBot);

        // Then
        verify(subscriptionService).subscribe(followerId, "John Doe", followingId);
        verify(userService).getUserByTelegramId(followerId);
        verify(userService).getUserByTelegramId(followingId);
        verify(sportBot).answerCallback(callbackQueryId, "Вы успешно подписались!");
        verify(sportBot).sendTgMessage(followerId, "✅ Вы подписались на пользователя: Jane Smith");
        verify(sportBot).sendTgMessage(followingId, "🔥 На вас подписался пользователь: John Doe");
    }

    @Test
    void handleSubscription_Success_WithoutLastName() {
        // Given
        Long followerId = 100001L;
        Long followingId = 100002L;
        String callbackData = "sub_100002";
        String callbackQueryId = "callback123";

        User currentUser = User.builder()
                .id(1)
                .telegramId(followerId)
                .fullName("John")
                .language("ru")
                .build();

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(callbackQueryId);
        when(telegramUser.getId()).thenReturn(followerId);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn(null);
        when(userService.getUserByTelegramId(followerId)).thenReturn(currentUser);
        when(userService.getUserByTelegramId(followingId)).thenReturn(dbUser);
        when(userService.getUserLocale(currentUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(userService.getUserLocale(dbUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(subscriptionService.subscribe(followerId, "John", followingId))
                .thenReturn("Вы подписались на Jane Smith");

        // When
        subscriptionHandler.handleSubscription(update, sportBot);

        // Then
        verify(subscriptionService).subscribe(followerId, "John", followingId);
        verify(userService).getUserByTelegramId(followerId);
        verify(userService).getUserByTelegramId(followingId);
        verify(sportBot).answerCallback(callbackQueryId, "Вы успешно подписались!");
        verify(sportBot).sendTgMessage(followerId, "✅ Вы подписались на пользователя: Jane Smith");
        verify(sportBot).sendTgMessage(followingId, "🔥 На вас подписался пользователь: John");
    }

    @Test
    void handleSubscription_ParsesCallbackDataCorrectly() {
        // Given
        Long followerId = 100001L;
        Long followingId = 999999L;
        String callbackData = "sub_999999";
        String callbackQueryId = "callback123";

        User currentUser = User.builder()
                .id(1)
                .telegramId(followerId)
                .fullName("John Doe")
                .language("ru")
                .build();

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(callbackQueryId);
        when(telegramUser.getId()).thenReturn(followerId);
        when(telegramUser.getFirstName()).thenReturn("John");
        when(telegramUser.getLastName()).thenReturn("Doe");
        when(userService.getUserByTelegramId(followerId)).thenReturn(currentUser);
        when(userService.getUserByTelegramId(followingId)).thenReturn(dbUser);
        when(userService.getUserLocale(currentUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(userService.getUserLocale(dbUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(subscriptionService.subscribe(followerId, "John Doe", followingId))
                .thenReturn("Вы подписались на Jane Smith");

        // When
        subscriptionHandler.handleSubscription(update, sportBot);

        // Then
        verify(subscriptionService).subscribe(followerId, "John Doe", followingId);
        verify(userService).getUserByTelegramId(followerId);
        verify(userService).getUserByTelegramId(followingId);
    }

    @Test
    void handleUnsubscription_Success() {
        // Given
        Long followerId = 100001L;
        Long followingId = 100002L;
        String callbackData = "unsub_100002";
        String callbackQueryId = "callback123";

        User currentUser = User.builder()
                .id(1)
                .telegramId(followerId)
                .fullName("John")
                .language("ru")
                .build();

        when(callbackQuery.getData()).thenReturn(callbackData);
        when(callbackQuery.getId()).thenReturn(callbackQueryId);
        when(telegramUser.getId()).thenReturn(followerId);
        when(userService.getUserByTelegramId(followerId)).thenReturn(currentUser);
        when(userService.getUserByTelegramId(followingId)).thenReturn(dbUser);
        when(userService.getUserLocale(currentUser)).thenReturn(Locale.forLanguageTag("ru"));
        when(subscriptionService.unsubscribe(followerId, followingId))
                .thenReturn("Вы отписались от Jane Smith");

        // When
        subscriptionHandler.handleUnsubscription(update, sportBot);

        // Then
        verify(subscriptionService).unsubscribe(followerId, followingId);
        verify(userService).getUserByTelegramId(followerId);
        verify(userService).getUserByTelegramId(followingId);
        verify(sportBot).answerCallback(callbackQueryId, "Вы успешно отписались!");
        verify(sportBot).sendTgMessage(followerId, "❌ Вы отписались от пользователя: Jane Smith");
    }
}
