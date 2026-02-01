package com.github.sportbot.bot;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


/**
 * TelegramMessageService
 */
@Slf4j
@Service
public class TelegramMessageService {

    private final SportBot sportBot;

    public TelegramMessageService(SportBot sportBot) {
        this.sportBot = sportBot;
    }


    public void sendTgMessage(Long telegramId, String text) {
        SendMessage message = new SendMessage(telegramId.toString(), text);
        try {
            sportBot.execute(message);
        } catch (TelegramApiException e) {
            log.error("TelegramApiException: Failed to send telegram message to {}: {}", telegramId, e.getMessage());
        }
    }
}
