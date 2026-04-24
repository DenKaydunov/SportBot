package com.github.sportbot.bot;

import com.github.sportbot.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class SportBot extends TelegramLongPollingBot {

    private static final String MENU_SUBSCRIPTION_TEXT = "Нажми на имя, чтобы подписаться на пользователя.";
    private static final String MENU_UNSUBSCRIPTION_TEXT = "Нажми на имя, чтобы отписаться от пользователя.";

    private final CallbackRouter callbackRouter;
    private final InlineKeyboardFactory keyboardFactory;

    public SportBot(@Value("${bot.token}") String botToken,
                    @Lazy CallbackRouter callbackRouter,
                    InlineKeyboardFactory keyboardFactory) {
        super(botToken);
        this.callbackRouter = callbackRouter;
        this.keyboardFactory = keyboardFactory;
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("Update received from telegram");
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleSimpleText(update);
        } else if (update.hasCallbackQuery()) {
            callbackRouter.route(update, this);
        }
    }

    public void sendSubscriptionMenu(Long telegramId, Page<UserResponse> userPage) {
        sendMenu(telegramId, userPage, MENU_SUBSCRIPTION_TEXT, true);
    }

    public void sendUnsubscriptionMenu(Long telegramId, Page<UserResponse> userPage) {
        sendMenu(telegramId, userPage, MENU_UNSUBSCRIPTION_TEXT, false);
    }

    /**
     * Универсальный метод для отправки меню с кнопками
     */
    private void sendMenu(Long telegramId, Page<UserResponse> userPage, String text, boolean isSubscriptionMenu) {
        SendMessage message = SendMessage.builder()
                .chatId(telegramId.toString())
                .text(text)
                .parseMode("Markdown")
                .replyMarkup(isSubscriptionMenu
                        ? keyboardFactory.getSubscriptionMenu(userPage)
                        : keyboardFactory.getUnsubscriptionMenu(userPage))
                .build();
        sendTgMessage(telegramId, message);
    }

    public void sendTgMessage(Long telegramId, String text) {
        sendTgMessage(telegramId, new SendMessage(telegramId.toString(), text));
    }

    public void sendTgMessage(Long telegramId, SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send message to {}: {}", telegramId, e.getMessage());
        }
    }

    public void answerCallback(String callbackQueryId, String text) {
        try {
            execute(AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Failed to answer callback: {}", e.getMessage());
        }
    }

    @Override
    public String getBotUsername() { return "Sport Bot"; }

    private void handleSimpleText(Update update) {
        //only for testing
//        sendTgMessage(update.getMessage().getFrom().getId(),
//                "Ты написал: " + update.getMessage().getText());
    }
}