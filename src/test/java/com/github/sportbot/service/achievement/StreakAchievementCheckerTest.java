package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StreakAchievementCheckerTest {

    private StreakAchievementChecker checker;
    private User testUser;
    private AchievementDefinition definition;

    @BeforeEach
    void setUp() {
        checker = new StreakAchievementChecker();

        testUser = new User();
        testUser.setCurrentStreak(10);

        definition = new AchievementDefinition();
        definition.setCategory(AchievementCategory.STREAK);
    }

    @Test
    void getCategory_shouldReturnStreak() {
        // When
        AchievementCategory category = checker.getCategory();

        // Then
        assertThat(category).isEqualTo(AchievementCategory.STREAK);
    }

    @Test
    void calculateProgress_whenUserHasStreak_shouldReturnCurrentStreak() {
        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(10);
    }

    @Test
    void calculateProgress_whenStreakIsZero_shouldReturnZero() {
        // Given
        testUser.setCurrentStreak(0);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }

    @Test
    void calculateProgress_whenUserIsNull_shouldReturnZero() {
        // When
        int progress = checker.calculateProgress(null, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }

    @Test
    void calculateProgress_whenCurrentStreakIsNull_shouldReturnZero() {
        // Given
        testUser.setCurrentStreak(null);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }
}
