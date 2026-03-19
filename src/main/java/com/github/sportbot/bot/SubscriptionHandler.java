package com.github.sportbot.bot;

import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.service.SubscriptionService;
import com.github.sportbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

/**
 * Обработчик подписок и отписок в Telegram боте
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionHandler {

    private static final String SUB_PREFIX = "sub_";
    private static final String UNSUB_PREFIX = "unsub_";

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final MessageSource messageSource;

    /**
     * Обрабатывает подписку на пользователя
     */
    public void handleSubscription(Update update, SportBot bot) {
        handleCallback(update, bot, SUB_PREFIX, true);
    }

    /**
     * Обрабатывает отписку от пользователя
     */
    public void handleUnsubscription(Update update, SportBot bot) {
        handleCallback(update, bot, UNSUB_PREFIX, false);
    }

    /**
     * Универсальный метод обработки callback'ов подписки/отписки
     */
    private void handleCallback(Update update, SportBot bot, String prefix, boolean isSubscribe) {
        try {
            CallbackData data = extractCallbackData(update, prefix);
            User currentUser = userService.getUserByTelegramId(data.userId);
            User targetUser = userService.getUserByTelegramId(data.targetUserId);

            if (isSubscribe) {
                subscriptionService.subscribe(data.userId, data.userName, data.targetUserId);
                sendSubscriptionMessages(bot, data, currentUser, targetUser);
            } else {
                subscriptionService.unsubscribe(data.userId, data.targetUserId);
                sendUnsubscriptionMessages(bot, data, currentUser, targetUser);
            }
        } catch (UserNotFoundException e) {
            Locale locale = getLocaleForCallback(update);
            log.error("User not found during {}: userId={}",
                    isSubscribe ? "subscription" : "unsubscription",
                    update.getCallbackQuery().getFrom().getId(), e);
            bot.answerCallback(update.getCallbackQuery().getId(),
                    localize("error.user.not.found", locale));
        } catch (NumberFormatException e) {
            Locale locale = getLocaleForCallback(update);
            log.error("Invalid callback data format: {}", update.getCallbackQuery().getData(), e);
            bot.answerCallback(update.getCallbackQuery().getId(),
                    localize("error.invalid.data", locale));
        } catch (Exception e) {
            Locale locale = getLocaleForCallback(update);
            log.error("Unexpected error during {}: userId={}",
                    isSubscribe ? "subscription" : "unsubscription",
                    update.getCallbackQuery().getFrom().getId(), e);
            String errorKey = isSubscribe ? "error.subscription.general" : "error.unsubscription.general";
            bot.answerCallback(update.getCallbackQuery().getId(),
                    localize(errorKey, locale));
        }
    }

    /**
     * Извлекает данные из callback query
     */
    private CallbackData extractCallbackData(Update update, String prefix) {
        String callbackData = update.getCallbackQuery().getData();
        String callbackQueryId = update.getCallbackQuery().getId();
        Long userId = update.getCallbackQuery().getFrom().getId();
        String userName = buildFullName(
                update.getCallbackQuery().getFrom().getFirstName(),
                update.getCallbackQuery().getFrom().getLastName()
        );

        if (!callbackData.startsWith(prefix)) {
            throw new IllegalArgumentException("Invalid callback data prefix: " + callbackData);
        }

        Long targetUserId = Long.parseLong(callbackData.replace(prefix, ""));

        return new CallbackData(userId, userName, targetUserId, callbackQueryId);
    }

    /**
     * Отправляет сообщения при подписке
     */
    private void sendSubscriptionMessages(SportBot bot, CallbackData data, User currentUser, User targetUser) {
        Locale currentUserLocale = userService.getUserLocale(currentUser);
        Locale targetUserLocale = userService.getUserLocale(targetUser);

        bot.answerCallback(data.callbackQueryId,
                localize("subscription.callback.success", currentUserLocale));
        bot.sendTgMessage(data.userId,
                localize("subscription.message.success", currentUserLocale, targetUser.getFullName()));
        bot.sendTgMessage(data.targetUserId,
                localize("subscription.notification", targetUserLocale, data.userName));
    }

    /**
     * Отправляет сообщения при отписке
     */
    private void sendUnsubscriptionMessages(SportBot bot, CallbackData data, User currentUser, User targetUser) {
        Locale currentUserLocale = userService.getUserLocale(currentUser);

        bot.answerCallback(data.callbackQueryId,
                localize("unsubscription.callback.success", currentUserLocale));
        bot.sendTgMessage(data.userId,
                localize("unsubscription.message.success", currentUserLocale, targetUser.getFullName()));
    }

    /**
     * Строит полное имя из firstName и lastName
     */
    private String buildFullName(String firstName, String lastName) {
        return firstName + (lastName != null ? " " + lastName : "");
    }

    /**
     * Attempts to resolve locale from update, defaults to Russian if user not found
     */
    private Locale getLocaleForCallback(Update update) {
        try {
            Long userId = update.getCallbackQuery().getFrom().getId();
            User user = userService.getUserByTelegramId(userId);
            return userService.getUserLocale(user);
        } catch (Exception e) {
            return Locale.forLanguageTag("ru");
        }
    }

    /**
     * Локализует сообщение без параметров
     */
    private String localize(String messageKey, Locale locale) {
        return messageSource.getMessage(messageKey, null, locale);
    }

    /**
     * Локализует сообщение с одним параметром
     */
    private String localize(String messageKey, Locale locale, String param) {
        return messageSource.getMessage(messageKey, new Object[]{param}, locale);
    }

    /**
     * Record для хранения данных из callback query
     */
    private record CallbackData(Long userId, String userName, Long targetUserId, String callbackQueryId) {
    }
}
