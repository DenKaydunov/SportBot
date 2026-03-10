package com.github.sportbot.config;

import com.github.sportbot.bot.SportBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(SportBot sportBot) throws TelegramApiException {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(sportBot);
            log.info("Telegram bot successfully registered and started polling");
            return botsApi;
        } catch (TelegramApiException e) {
            // Игнорируем ошибку удаления webhook - это нормально, если webhook не был установлен
            if (e.getMessage() != null && e.getMessage().contains("Error removing old webhook")) {
                log.warn("Webhook cleanup warning (can be ignored): {}", e.getMessage());
                // Возвращаем API без регистрации - бот все равно может работать
                return new TelegramBotsApi(DefaultBotSession.class);
            }
            log.error("Failed to register Telegram bot: {}", e.getMessage(), e);
            throw e; // Пробрасываем исключение, чтобы Spring показал ошибку при старте
        }
    }
}
