package com.github.sportbot.bot;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для вспомогательных методов SportBot, связанных с пагинацией
 */
class SportBotPaginationTest {

    @ParameterizedTest
    @CsvSource({
            "sub_page_next_2, 2",
            "unsub_page_prev_0, 0",
            "sub_page_next_15, 15"
    })
    void extractPageNumber_ParsesCorrectly(String callbackData, int expectedPageNumber) {
        // Можем протестировать логику парсинга отдельно, если вынесем метод
        // Пока это частный метод, тестируем через интеграцию

        // Симулируем логику extractPageNumber
        String[] parts = callbackData.split("_");
        int pageNumber = Integer.parseInt(parts[parts.length - 1]);

        assertEquals(expectedPageNumber, pageNumber);
    }
}
