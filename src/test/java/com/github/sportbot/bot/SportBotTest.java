package com.github.sportbot.bot;

import com.github.sportbot.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SportBotTest {

    @Mock
    private CallbackRouter callbackRouter;

    @Mock
    private InlineKeyboardFactory keyboardFactory;

    @Mock
    private InlineKeyboardMarkup keyboardMarkup;

    private SportBot sportBot;

    @BeforeEach
    void setUp() {
        sportBot = spy(new SportBot("test-token", callbackRouter, keyboardFactory));
    }

    @Test
    void onUpdateReceived_withTextMessage_shouldHandleSimpleText() {
        // Given
        Update update = new Update();
        Message message = new Message();
        message.setText("Hello");
        User user = new User();
        user.setId(123456L);
        message.setFrom(user);
        update.setMessage(message);

        // When
        sportBot.onUpdateReceived(update);

        // Then
        verify(callbackRouter, never()).route(any(), any());
        // handleSimpleText is currently empty/commented, so nothing happens
    }

    @Test
    void onUpdateReceived_withCallbackQuery_shouldRouteToCallbackRouter() {
        // Given
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("test_callback");
        update.setCallbackQuery(callbackQuery);

        // When
        sportBot.onUpdateReceived(update);

        // Then
        verify(callbackRouter).route(update, sportBot);
    }

    @Test
    void sendSubscriptionMenu_shouldSendMenuWithCorrectText() {
        // Given
        Long telegramId = 123456L;
        UserResponse user1 = new UserResponse("msg", 1L, "User1");
        Page<UserResponse> userPage = new PageImpl<>(List.of(user1));

        when(keyboardFactory.getSubscriptionMenu(userPage)).thenReturn(keyboardMarkup);
        doNothing().when(sportBot).sendTgMessage(anyLong(), any(SendMessage.class));

        // When
        sportBot.sendSubscriptionMenu(telegramId, userPage);

        // Then
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(sportBot).sendTgMessage(eq(telegramId), messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(telegramId.toString());
        assertThat(sentMessage.getText()).isEqualTo("Нажми на имя, чтобы подписаться на пользователя.");
        assertThat(sentMessage.getParseMode()).isEqualTo("Markdown");
        assertThat(sentMessage.getReplyMarkup()).isEqualTo(keyboardMarkup);

        verify(keyboardFactory).getSubscriptionMenu(userPage);
    }

    @Test
    void sendUnsubscriptionMenu_shouldSendMenuWithCorrectText() {
        // Given
        Long telegramId = 123456L;
        UserResponse user1 = new UserResponse("msg", 1L, "User1");
        Page<UserResponse> userPage = new PageImpl<>(List.of(user1));

        when(keyboardFactory.getUnsubscriptionMenu(userPage)).thenReturn(keyboardMarkup);
        doNothing().when(sportBot).sendTgMessage(anyLong(), any(SendMessage.class));

        // When
        sportBot.sendUnsubscriptionMenu(telegramId, userPage);

        // Then
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(sportBot).sendTgMessage(eq(telegramId), messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(telegramId.toString());
        assertThat(sentMessage.getText()).isEqualTo("Нажми на имя, чтобы отписаться от пользователя.");
        assertThat(sentMessage.getParseMode()).isEqualTo("Markdown");
        assertThat(sentMessage.getReplyMarkup()).isEqualTo(keyboardMarkup);

        verify(keyboardFactory).getUnsubscriptionMenu(userPage);
    }

    @Test
    void sendTgMessage_withStringText_shouldCreateAndSendMessage() {
        // Given
        Long telegramId = 123456L;
        String text = "Test message";

        // Reset spy to use real implementation for this test
        SportBot realBot = new SportBot("test-token", callbackRouter, keyboardFactory);
        SportBot spyBot = spy(realBot);
        doNothing().when(spyBot).sendTgMessage(anyLong(), any(SendMessage.class));

        // When
        spyBot.sendTgMessage(telegramId, text);

        // Then
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(spyBot).sendTgMessage(eq(telegramId), messageCaptor.capture());

        SendMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getChatId()).isEqualTo(telegramId.toString());
        assertThat(sentMessage.getText()).isEqualTo(text);
    }

    @Test
    void getBotUsername_shouldReturnCorrectName() {
        // When
        String username = sportBot.getBotUsername();

        // Then
        assertThat(username).isEqualTo("Sport Bot");
    }

    @Test
    void answerCallback_shouldExecuteAnswerCallbackQuery() throws TelegramApiException {
        // Given
        String callbackQueryId = "callback-123";
        String text = "Callback answered";

        doReturn(null).when(sportBot).execute(any(AnswerCallbackQuery.class));

        // When
        sportBot.answerCallback(callbackQueryId, text);

        // Then
        ArgumentCaptor<AnswerCallbackQuery> captor = ArgumentCaptor.forClass(AnswerCallbackQuery.class);
        verify(sportBot).execute(captor.capture());

        AnswerCallbackQuery answer = captor.getValue();
        assertThat(answer.getCallbackQueryId()).isEqualTo(callbackQueryId);
        assertThat(answer.getText()).isEqualTo(text);
    }
}
