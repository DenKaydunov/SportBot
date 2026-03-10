package com.github.sportbot.bot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Маршрутизатор для обработки callback запросов от Telegram
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CallbackRouter {

    private final SubscriptionHandler subscriptionHandler;
    private final PaginationHandler paginationHandler;

    /**
     * Маршрутизирует callback query к соответствующему обработчику
     */
    public void route(Update update, SportBot bot) {
        String data = update.getCallbackQuery().getData();
        log.debug("Routing callback: {}", data);

        if (isPaginationCallback(data)) {
            paginationHandler.handlePagination(update, bot);
        } else if (isSubscriptionCallback(data)) {
            subscriptionHandler.handleSubscription(update, bot);
        } else if (isUnsubscriptionCallback(data)) {
            subscriptionHandler.handleUnsubscription(update, bot);
        } else if (isPageIndicatorCallback(data)) {
            bot.answerCallback(update.getCallbackQuery().getId(), "Текущая страница");
        } else {
            log.warn("Unknown callback data: {}", data);
            bot.answerCallback(update.getCallbackQuery().getId(), "Неизвестная команда");
        }
    }

    private boolean isPaginationCallback(String data) {
        return data.contains("page_next_") || data.contains("page_prev_");
    }

    private boolean isSubscriptionCallback(String data) {
        return data.startsWith("sub_") && !isPaginationCallback(data);
    }

    private boolean isUnsubscriptionCallback(String data) {
        return data.startsWith("unsub_") && !isPaginationCallback(data);
    }

    private boolean isPageIndicatorCallback(String data) {
        return data.equals("page_current");
    }
}
