package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralAchievementCheckerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReferralAchievementChecker checker;

    private User testUser;
    private AchievementDefinition definition;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setTelegramId(123456L);

        definition = new AchievementDefinition();
        definition.setCategory(AchievementCategory.REFERRAL);
    }

    @Test
    void getCategory_shouldReturnReferral() {
        // When
        AchievementCategory category = checker.getCategory();

        // Then
        assertThat(category).isEqualTo(AchievementCategory.REFERRAL);
    }

    @Test
    void calculateProgress_whenUserHasReferrals_shouldReturnCount() {
        // Given
        when(userRepository.countByReferrerTelegramId(123456L)).thenReturn(5);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(5);
    }

    @Test
    void calculateProgress_whenUserHasNoReferrals_shouldReturnZero() {
        // Given
        when(userRepository.countByReferrerTelegramId(123456L)).thenReturn(0);

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
    void calculateProgress_whenTelegramIdIsNull_shouldReturnZero() {
        // Given
        testUser.setTelegramId(null);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }
}
