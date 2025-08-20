package com.github.sportbot.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LeaderboardServiceIntegrationTest {

    @Autowired
    private LeaderboardService leaderboardService;

    @Test
    void getLeaderboardString_ForPushup_ReturnsCorrectData() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 5);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        assertTrue(result.contains("Всего пользователи сделали:"));
        
        // Проверяем, что возвращается ровно 5 записей
        long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
        assertEquals(5, lineCount);
        
        // Проверяем формат строк
        assertTrue(result.lines().anyMatch(line -> line.matches("\\d+\\. Test User \\d+ - \\d+ • max \\d+")));
    }

    @Test
    void getLeaderboardString_ForSquat_ReturnsCorrectData() {
        // When
        String result = leaderboardService.getLeaderboardString("squat", 10);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        
        // Проверяем, что возвращается ровно 10 записей
        long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
        assertEquals(10, lineCount);
        
        // Проверяем, что записи отсортированы по убыванию
        String[] lines = result.lines().filter(line -> line.matches("\\d+\\..*")).toArray(String[]::new);
        for (int i = 0; i < lines.length - 1; i++) {
            int currentTotal = extractTotalFromLine(lines[i]);
            int nextTotal = extractTotalFromLine(lines[i + 1]);
            assertTrue(currentTotal >= nextTotal, 
                "Line " + (i + 1) + " should be >= line " + (i + 2) + ": " + currentTotal + " vs " + nextTotal);
        }
    }

    @Test
    void getLeaderboardString_ForPlank_ReturnsCorrectData() {
        // When
        String result = leaderboardService.getLeaderboardString("plank", 15);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
        
        // Проверяем, что возвращается ровно 15 записей
        long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
        assertEquals(15, lineCount);
    }

    @Test
    void getLeaderboardString_WithLimit2_ReturnsOnly2Entries() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 2);

        // Then
        assertNotNull(result);
        
        // Проверяем, что возвращается ровно 2 записи
        long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
        assertEquals(2, lineCount);
        
        // Проверяем, что нет третьей записи
        assertFalse(result.contains("3."));
    }

    @Test
    void getLeaderboardString_WithLargeLimit_ReturnsAllEntries() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 100);

        // Then
        assertNotNull(result);
        
        // Проверяем, что возвращается максимум 20 записей (всего 20 пользователей)
        long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
        assertTrue(lineCount <= 20, "Should return at most 20 entries, got: " + lineCount);
        assertTrue(lineCount > 0, "Should return at least 1 entry");
    }

    @Test
    void getLeaderboardString_AllExerciseTypes_WorkCorrectly() {
        // Test all exercise types
        String[] exerciseTypes = {"pushup", "squat", "plank"};
        
        for (String exerciseType : exerciseTypes) {
            // When
            String result = leaderboardService.getLeaderboardString(exerciseType, 5);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("⚡Таблица лидеров⚡"));
            assertTrue(result.contains(exerciseType.toLowerCase()));
            
            long lineCount = result.lines().filter(line -> line.matches("\\d+\\..*")).count();
            assertEquals(5, lineCount);
        }
    }

    @Test
    void getLeaderboardString_ContainsValidData() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 3);

        // Then
        assertNotNull(result);
        
        // Проверяем структуру каждой строки
        result.lines()
            .filter(line -> line.matches("\\d+\\..*"))
            .forEach(line -> {
                assertTrue(line.matches("\\d+\\. Test User \\d+ - \\d+ • max \\d+"), 
                    "Invalid line format: " + line);
                
                // Проверяем, что числа положительные
                int total = extractTotalFromLine(line);
                int max = extractMaxFromLine(line);
                assertTrue(total > 0, "Total should be positive: " + total);
                assertTrue(max >= 0, "Max should be non-negative: " + max);
            });
    }

    @Test
    void getLeaderboardString_WithoutPeriod_ShowsAllTimePeriod() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 5);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Период: За все время"));
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
    }

    @Test
    void getLeaderboardString_WithNullPeriod_ShowsAllTimePeriod() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 5, null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Период: За все время"));
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
    }

    @Test
    void getLeaderboardString_WithInvalidPeriod_ShowsAllTimePeriod() {
        // When
        String result = leaderboardService.getLeaderboardString("pushup", 5, "invalid");

        // Then
        assertNotNull(result);
        assertTrue(result.contains("Период: За все время"));
        assertTrue(result.contains("⚡Таблица лидеров⚡"));
    }

    // Вспомогательные методы для извлечения данных из строк
    private int extractTotalFromLine(String line) {
        String[] parts = line.split(" - ");
        if (parts.length >= 2) {
            String totalPart = parts[1].split(" •")[0];
            return Integer.parseInt(totalPart.trim());
        }
        return 0;
    }

    private int extractMaxFromLine(String line) {
        String[] parts = line.split(" • max ");
        if (parts.length >= 2) {
            return Integer.parseInt(parts[1].trim());
        }
        return 0;
    }
}
