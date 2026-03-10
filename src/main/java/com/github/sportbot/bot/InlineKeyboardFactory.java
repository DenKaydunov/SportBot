package com.github.sportbot.bot;

import com.github.sportbot.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InlineKeyboardFactory {

    private static final String SUBSCRIBE_PREFIX = "sub_";
    private static final String UNSUBSCRIBE_PREFIX = "unsub_";
    private static final String PAGE_NEXT = "page_next_";
    private static final String PAGE_PREV = "page_prev_";

    private static final String EMOJI_ADD = "➕";
    private static final String EMOJI_REMOVE = "➖";
    private static final String EMOJI_NEXT = "▶️";
    private static final String EMOJI_PREV = "◀️";

    public InlineKeyboardMarkup getSubscriptionMenu(Page<UserResponse> userPage) {
        return buildUserMenu(userPage, SUBSCRIBE_PREFIX, EMOJI_ADD, "subscription");
    }

    public InlineKeyboardMarkup getUnsubscriptionMenu(Page<UserResponse> userPage) {
        return buildUserMenu(userPage, UNSUBSCRIBE_PREFIX, EMOJI_REMOVE, "unsubscription");
    }

    /**
     * Универсальный метод для построения меню с пользователями
     * @param userPage страница с пользователями
     * @param callbackPrefix префикс для callback data
     * @param emoji эмодзи для кнопки
     * @param menuType тип меню для логирования
     */
    private InlineKeyboardMarkup buildUserMenu(Page<UserResponse> userPage,
                                               String callbackPrefix,
                                               String emoji,
                                               String menuType) {
        if (userPage.isEmpty()) {
            log.warn("Empty user page for {} menu", menuType);
            return InlineKeyboardMarkup.builder()
                    .keyboard(List.of())
                    .build();
        }

        List<List<InlineKeyboardButton>> rows = userPage.getContent().stream()
                .map(user -> createUserButton(user, callbackPrefix, emoji))
                .collect(Collectors.toList());

        // Добавляем кнопки пагинации, если есть несколько страниц
        if (userPage.getTotalPages() > 1) {
            rows.add(createPaginationRow(userPage, callbackPrefix));
        }

        log.info("Built {} menu with {} users, page {}/{}",
                menuType, userPage.getNumberOfElements(),
                userPage.getNumber() + 1, userPage.getTotalPages());

        return InlineKeyboardMarkup.builder()
                .keyboard(rows)
                .build();
    }

    /**
     * Создает кнопку для пользователя
     */
    private List<InlineKeyboardButton> createUserButton(UserResponse user,
                                                        String callbackPrefix,
                                                        String emoji) {
        return List.of(
                InlineKeyboardButton.builder()
                        .text(emoji + " " + user.fullName())
                        .callbackData(callbackPrefix + user.telegramId())
                        .build()
        );
    }

    /**
     * Создает ряд с кнопками пагинации
     */
    private List<InlineKeyboardButton> createPaginationRow(Page<?> page, String callbackPrefix) {
        List<InlineKeyboardButton> paginationButtons = new ArrayList<>();

        // Кнопка "Назад"
        if (page.hasPrevious()) {
            paginationButtons.add(
                    InlineKeyboardButton.builder()
                            .text(EMOJI_PREV + " Назад")
                            .callbackData(callbackPrefix + PAGE_PREV + (page.getNumber() - 1))
                            .build()
            );
        }

        // Индикатор текущей страницы
        paginationButtons.add(
                InlineKeyboardButton.builder()
                        .text(String.format("%d/%d", page.getNumber() + 1, page.getTotalPages()))
                        .callbackData("page_current") // Не делает ничего, просто показывает номер
                        .build()
        );

        // Кнопка "Вперед"
        if (page.hasNext()) {
            paginationButtons.add(
                    InlineKeyboardButton.builder()
                            .text("Вперед " + EMOJI_NEXT)
                            .callbackData(callbackPrefix + PAGE_NEXT + (page.getNumber() + 1))
                            .build()
            );
        }

        return paginationButtons;
    }
}