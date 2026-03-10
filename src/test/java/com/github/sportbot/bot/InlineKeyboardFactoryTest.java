package com.github.sportbot.bot;

import com.github.sportbot.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InlineKeyboardFactoryTest {

    private InlineKeyboardFactory keyboardFactory;

    @BeforeEach
    void setUp() {
        keyboardFactory = new InlineKeyboardFactory();
    }

    @Test
    void getSubscriptionMenu_WithUsers_CreatesButtonsWithPlusEmoji() {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse("Success", 100001L, "John Doe"),
                new UserResponse("Success", 100002L, "Jane Smith")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, users.size());

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getSubscriptionMenu(userPage);

        // Then
        assertNotNull(keyboard);
        assertEquals(2, keyboard.getKeyboard().size());

        // Проверяем первую кнопку
        InlineKeyboardButton firstButton = keyboard.getKeyboard().get(0).get(0);
        assertEquals("➕ John Doe", firstButton.getText());
        assertEquals("sub_100001", firstButton.getCallbackData());

        // Проверяем вторую кнопку
        InlineKeyboardButton secondButton = keyboard.getKeyboard().get(1).get(0);
        assertEquals("➕ Jane Smith", secondButton.getText());
        assertEquals("sub_100002", secondButton.getCallbackData());
    }

    @Test
    void getUnsubscriptionMenu_WithUsers_CreatesButtonsWithMinusEmoji() {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse("Success", 100001L, "John Doe")
        );
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, users.size());

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getUnsubscriptionMenu(userPage);

        // Then
        assertNotNull(keyboard);
        assertEquals(1, keyboard.getKeyboard().size());

        InlineKeyboardButton button = keyboard.getKeyboard().get(0).get(0);
        assertEquals("➖ John Doe", button.getText());
        assertEquals("unsub_100001", button.getCallbackData());
    }

    @Test
    void getSubscriptionMenu_WithEmptyPage_ReturnsEmptyKeyboard() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserResponse> userPage = new PageImpl<>(List.of(), pageable, 0);

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getSubscriptionMenu(userPage);

        // Then
        assertNotNull(keyboard);
        assertTrue(keyboard.getKeyboard().isEmpty());
    }

    @Test
    void getSubscriptionMenu_WithMultiplePages_AddsPaginationButtons() {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse("Success", 100001L, "User 1"),
                new UserResponse("Success", 100002L, "User 2")
        );
        Pageable pageable = PageRequest.of(1, 2); // Вторая страница из 3
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, 6); // Всего 6 пользователей

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getSubscriptionMenu(userPage);

        // Then
        assertNotNull(keyboard);
        assertEquals(3, keyboard.getKeyboard().size()); // 2 пользователя + 1 ряд пагинации

        // Проверяем ряд пагинации (последний ряд)
        List<InlineKeyboardButton> paginationRow = keyboard.getKeyboard().get(2);
        assertTrue(paginationRow.size() >= 2); // Минимум кнопка назад и индикатор

        // Проверяем наличие кнопки "Назад"
        boolean hasPrevButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Назад"));
        assertTrue(hasPrevButton);

        // Проверяем наличие кнопки "Вперед"
        boolean hasNextButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Вперед"));
        assertTrue(hasNextButton);

        // Проверяем индикатор страницы
        boolean hasPageIndicator = paginationRow.stream()
                .anyMatch(btn -> btn.getText().matches("\\d+/\\d+"));
        assertTrue(hasPageIndicator);
    }

    @Test
    void getSubscriptionMenu_FirstPage_HasOnlyNextButton() {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse("Success", 100001L, "User 1")
        );
        Pageable pageable = PageRequest.of(0, 1);
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, 2); // Первая из 2 страниц

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getSubscriptionMenu(userPage);

        // Then
        List<InlineKeyboardButton> paginationRow = keyboard.getKeyboard().get(1);

        // Проверяем, что нет кнопки "Назад"
        boolean hasPrevButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Назад"));
        assertFalse(hasPrevButton);

        // Проверяем наличие кнопки "Вперед"
        boolean hasNextButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Вперед"));
        assertTrue(hasNextButton);
    }

    @Test
    void getSubscriptionMenu_LastPage_HasOnlyPrevButton() {
        // Given
        List<UserResponse> users = Arrays.asList(
                new UserResponse("Success", 100001L, "User 1")
        );
        Pageable pageable = PageRequest.of(1, 1); // Последняя страница
        Page<UserResponse> userPage = new PageImpl<>(users, pageable, 2);

        // When
        InlineKeyboardMarkup keyboard = keyboardFactory.getSubscriptionMenu(userPage);

        // Then
        List<InlineKeyboardButton> paginationRow = keyboard.getKeyboard().get(1);

        // Проверяем наличие кнопки "Назад"
        boolean hasPrevButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Назад"));
        assertTrue(hasPrevButton);

        // Проверяем, что нет кнопки "Вперед"
        boolean hasNextButton = paginationRow.stream()
                .anyMatch(btn -> btn.getText().contains("Вперед"));
        assertFalse(hasNextButton);
    }
}
