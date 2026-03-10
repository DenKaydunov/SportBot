package com.github.sportbot.bot;

import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.model.User;
import com.github.sportbot.service.SubscriptionService;
import com.github.sportbot.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Обработчик пагинации для меню подписок и отписок
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaginationHandler {

    private static final int PAGE_SIZE = 10;
    private static final String MENU_SUBSCRIPTION_TEXT = "Нажми на имя, чтобы подписаться на пользователя.";
    private static final String MENU_UNSUBSCRIPTION_TEXT = "Нажми на имя, чтобы отписаться от пользователя.";

    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final InlineKeyboardFactory keyboardFactory;

    /**
     * Обрабатывает callback пагинации
     */
    public void handlePagination(Update update, SportBot bot) {
        try {
            String data = update.getCallbackQuery().getData();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            Long userId = update.getCallbackQuery().getFrom().getId();

            boolean isSubscriptionMenu = data.startsWith("sub_");
            int pageNumber = extractPageNumber(data);

            Page<UserResponse> userPage = isSubscriptionMenu
                    ? getSubscriptionPage(pageNumber)
                    : getUnsubscriptionPage(userId, pageNumber);

            String menuText = isSubscriptionMenu
                    ? MENU_SUBSCRIPTION_TEXT
                    : MENU_UNSUBSCRIPTION_TEXT;

            EditMessageText editMessage = EditMessageText.builder()
                    .chatId(chatId.toString())
                    .messageId(messageId)
                    .text(menuText)
                    .parseMode("Markdown")
                    .replyMarkup(isSubscriptionMenu
                            ? keyboardFactory.getSubscriptionMenu(userPage)
                            : keyboardFactory.getUnsubscriptionMenu(userPage))
                    .build();

            bot.execute(editMessage);
            bot.answerCallback(update.getCallbackQuery().getId(), "Страница " + (pageNumber + 1));

        } catch (TelegramApiException e) {
            log.error("Failed to handle pagination: {}", e.getMessage(), e);
            bot.answerCallback(update.getCallbackQuery().getId(), "Ошибка при переключении страницы");
        } catch (Exception e) {
            log.error("Unexpected error in pagination: {}", e.getMessage(), e);
            bot.answerCallback(update.getCallbackQuery().getId(), "Произошла ошибка");
        }
    }

    /**
     * Извлекает номер страницы из callback data
     * Формат: "sub_page_next_2" или "unsub_page_prev_0"
     */
    private int extractPageNumber(String callbackData) {
        try {
            String[] parts = callbackData.split("_");
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (Exception e) {
            log.error("Failed to parse page number from callback: {}", callbackData, e);
            return 0;
        }
    }

    /**
     * Получает страницу всех пользователей для меню подписок
     */
    private Page<UserResponse> getSubscriptionPage(int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"));
        return userService.getAllUsersPaged(pageable);
    }

    /**
     * Получает страницу пользователей для меню отписок (только те, на кого подписан)
     */
    private Page<UserResponse> getUnsubscriptionPage(Long userId, int pageNumber) {
        List<User> following = subscriptionService.getFollowing(userId);
        List<UserResponse> userResponses = following.stream()
                .map(user -> new UserResponse("Success", user.getTelegramId(), user.getFullName()))
                .toList();

        int start = pageNumber * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, userResponses.size());
        List<UserResponse> pageContent = userResponses.subList(
                Math.min(start, userResponses.size()),
                end
        );

        Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "createdAt"));
        return new PageImpl<>(
                pageContent,
                pageable,
                userResponses.size()
        );
    }
}
