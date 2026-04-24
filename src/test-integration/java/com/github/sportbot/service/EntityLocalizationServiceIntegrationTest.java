package com.github.sportbot.service;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.repository.AchievementDefinitionRepository;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.RankRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class EntityLocalizationServiceIntegrationTest {

    @Autowired
    private EntityLocalizationService entityLocalizationService;

    @Autowired
    private ExerciseTypeRepository exerciseTypeRepository;

    @Autowired
    private RankRepository rankRepository;

    @Autowired
    private AchievementDefinitionRepository achievementDefinitionRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void testGetExerciseTypeTitleForAllTypes() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When - Load push_up
        ExerciseType pushUp = exerciseTypeRepository.findByCode("push_up")
                .orElseThrow(() -> new AssertionError("push_up not found in database"));

        // Then
        assertThat(entityLocalizationService.getExerciseTypeTitle(pushUp, ruLocale))
                .isEqualTo("Отжимания");
        assertThat(entityLocalizationService.getExerciseTypeTitle(pushUp, enLocale))
                .isEqualTo("Push-ups");
        assertThat(entityLocalizationService.getExerciseTypeTitle(pushUp, ukLocale))
                .isEqualTo("Віджимання");

        // When - Load pull_up
        ExerciseType pullUp = exerciseTypeRepository.findByCode("pull_up")
                .orElseThrow(() -> new AssertionError("pull_up not found in database"));

        // Then
        assertThat(entityLocalizationService.getExerciseTypeTitle(pullUp, ruLocale))
                .isEqualTo("Подтягивания");
        assertThat(entityLocalizationService.getExerciseTypeTitle(pullUp, enLocale))
                .isEqualTo("Pull-ups");
        assertThat(entityLocalizationService.getExerciseTypeTitle(pullUp, ukLocale))
                .isEqualTo("Підтягування");

        // When - Load squat
        ExerciseType squat = exerciseTypeRepository.findByCode("squat")
                .orElseThrow(() -> new AssertionError("squat not found in database"));

        // Then
        assertThat(entityLocalizationService.getExerciseTypeTitle(squat, ruLocale))
                .isEqualTo("Приседания");
        assertThat(entityLocalizationService.getExerciseTypeTitle(squat, enLocale))
                .isEqualTo("Squats");
        assertThat(entityLocalizationService.getExerciseTypeTitle(squat, ukLocale))
                .isEqualTo("Присідання");

        // When - Load abs
        ExerciseType abs = exerciseTypeRepository.findByCode("abs")
                .orElseThrow(() -> new AssertionError("abs not found in database"));

        // Then
        assertThat(entityLocalizationService.getExerciseTypeTitle(abs, ruLocale))
                .isEqualTo("Пресс");
        assertThat(entityLocalizationService.getExerciseTypeTitle(abs, enLocale))
                .isEqualTo("Abs");
        assertThat(entityLocalizationService.getExerciseTypeTitle(abs, ukLocale))
                .isEqualTo("Прес");
    }

    @Test
    void testGetExerciseTypeTitleFallbackToDbValue() {
        // Given
        ExerciseType testExercise = ExerciseType.builder()
                .code("test_exercise")
                .title("Тестовое упражнение")
                .build();

        // When
        ExerciseType savedExercise = exerciseTypeRepository.saveAndFlush(testExercise);
        String result = entityLocalizationService.getExerciseTypeTitle(
                savedExercise,
                Locale.forLanguageTag("en")
        );

        // Then - Should fallback to DB title since key doesn't exist in properties
        assertThat(result).isEqualTo("Тестовое упражнение");
    }

    @Test
    void testGetRankTitleForSampleRanks() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When & Then - seed
        Rank seed = findRankByCodeOrSkip("seed");
        if (seed != null) {
            assertThat(entityLocalizationService.getRankTitle(seed, ruLocale))
                    .isEqualTo("Зерно");
            assertThat(entityLocalizationService.getRankTitle(seed, enLocale))
                    .isEqualTo("Seed");
            assertThat(entityLocalizationService.getRankTitle(seed, ukLocale))
                    .isEqualTo("Зерно");
        }

        // When & Then - sprout
        Rank sprout = findRankByCodeOrSkip("sprout");
        if (sprout != null) {
            assertThat(entityLocalizationService.getRankTitle(sprout, ruLocale))
                    .isEqualTo("Росток");
            assertThat(entityLocalizationService.getRankTitle(sprout, enLocale))
                    .isEqualTo("Sprout");
            assertThat(entityLocalizationService.getRankTitle(sprout, ukLocale))
                    .isEqualTo("Росток");
        }

        // When & Then - bogatyr
        Rank bogatyr = findRankByCodeOrSkip("bogatyr");
        if (bogatyr != null) {
            assertThat(entityLocalizationService.getRankTitle(bogatyr, ruLocale))
                    .isEqualTo("Богатырь");
            assertThat(entityLocalizationService.getRankTitle(bogatyr, enLocale))
                    .isEqualTo("Bogatyr");
            assertThat(entityLocalizationService.getRankTitle(bogatyr, ukLocale))
                    .isEqualTo("Богатир");
        }

        // When & Then - deity
        Rank deity = findRankByCodeOrSkip("deity");
        if (deity != null) {
            assertThat(entityLocalizationService.getRankTitle(deity, ruLocale))
                    .isEqualTo("Божество");
            assertThat(entityLocalizationService.getRankTitle(deity, enLocale))
                    .isEqualTo("Deity");
            assertThat(entityLocalizationService.getRankTitle(deity, ukLocale))
                    .isEqualTo("Божество");
        }
    }

    @Test
    void testGetRankTitleForAllRanks() {
        // Given
        List<Rank> allRanks = rankRepository.findAll();
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // Then - Should have ranks in database
        assertThat(allRanks).isNotEmpty();

        int differentTranslationsCount = 0;

        // When & Then - Each rank should have non-empty localized titles
        for (Rank rank : allRanks) {
            String ruTitle = entityLocalizationService.getRankTitle(rank, ruLocale);
            String enTitle = entityLocalizationService.getRankTitle(rank, enLocale);
            String ukTitle = entityLocalizationService.getRankTitle(rank, ukLocale);

            assertThat(ruTitle).isNotEmpty();
            assertThat(enTitle).isNotEmpty();
            assertThat(ukTitle).isNotEmpty();

            // Count how many ranks have different translations
            if (!ruTitle.equals(enTitle) || !ruTitle.equals(ukTitle)) {
                differentTranslationsCount++;
            }
        }

        // At least some ranks should have different translations across languages
        assertThat(differentTranslationsCount).isGreaterThanOrEqualTo(5);
    }

    @Test
    void testGetRankTitleFallbackToDbValue() {
        // Given
        Rank testRank = Rank.builder()
                .code("test_rank")
                .title("Тестовый ранг")
                .threshold(999999)
                .createdAt(LocalDateTime.now())
                .build();

        // When
        Rank savedRank = rankRepository.saveAndFlush(testRank);
        String result = entityLocalizationService.getRankTitle(
                savedRank,
                Locale.forLanguageTag("en")
        );

        // Then - Should fallback to DB title since key doesn't exist
        assertThat(result).isEqualTo("Тестовый ранг");
    }

    @Test
    void testGetAchievementTitleForStreakAchievements() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When & Then - STREAK_10_DAYS
        AchievementDefinition streak10 = findAchievementByCodeOrSkip("STREAK_10_DAYS");
        if (streak10 != null) {
            String ruTitle = entityLocalizationService.getAchievementTitle(streak10, ruLocale);
            String enTitle = entityLocalizationService.getAchievementTitle(streak10, enLocale);
            String ukTitle = entityLocalizationService.getAchievementTitle(streak10, ukLocale);

            assertThat(ruTitle).isNotEmpty();
            assertThat(enTitle).isNotEmpty();
            assertThat(ukTitle).isNotEmpty();
        }

        // When & Then - STREAK_20_DAYS
        AchievementDefinition streak20 = findAchievementByCodeOrSkip("STREAK_20_DAYS");
        if (streak20 != null) {
            String ruTitle = entityLocalizationService.getAchievementTitle(streak20, ruLocale);
            String enTitle = entityLocalizationService.getAchievementTitle(streak20, enLocale);

            assertThat(ruTitle).isNotEmpty();
            assertThat(enTitle).isNotEmpty();
        }

        // When & Then - STREAK_50_DAYS
        AchievementDefinition streak50 = findAchievementByCodeOrSkip("STREAK_50_DAYS");
        if (streak50 != null) {
            String ruTitle = entityLocalizationService.getAchievementTitle(streak50, ruLocale);
            assertThat(ruTitle).isNotEmpty();
        }
    }

    @Test
    void testGetAchievementDescriptionForStreakAchievements() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");
        Locale enLocale = Locale.forLanguageTag("en");
        Locale ukLocale = Locale.forLanguageTag("uk");

        // When - Load streak achievements
        AchievementDefinition streak10 = findAchievementByCodeOrSkip("STREAK_10_DAYS");
        if (streak10 != null) {
            String ruDesc = entityLocalizationService.getAchievementDescription(streak10, ruLocale);
            String enDesc = entityLocalizationService.getAchievementDescription(streak10, enLocale);
            String ukDesc = entityLocalizationService.getAchievementDescription(streak10, ukLocale);

            // Then - Descriptions should be non-empty
            assertThat(ruDesc).isNotEmpty();
            assertThat(enDesc).isNotEmpty();
            assertThat(ukDesc).isNotEmpty();
        }

        AchievementDefinition streak20 = findAchievementByCodeOrSkip("STREAK_20_DAYS");
        if (streak20 != null) {
            String ruDesc = entityLocalizationService.getAchievementDescription(streak20, ruLocale);
            String enDesc = entityLocalizationService.getAchievementDescription(streak20, enLocale);

            assertThat(ruDesc).isNotEmpty();
            assertThat(enDesc).isNotEmpty();
        }
    }

    @Test
    void testGetAchievementTitleFallbackToCode() {
        // Given
        AchievementDefinition testAchievement = AchievementDefinition.builder()
                .code("TEST_ACHIEVEMENT")
                .category(AchievementCategory.STREAK)
                .emoji("🏆")
                .targetValue(100)
                .rewardTon(10)
                .isActive(true)
                .isLegendary(false)
                .sortOrder(999)
                .build();

        // When
        AchievementDefinition saved = achievementDefinitionRepository.saveAndFlush(testAchievement);
        String result = entityLocalizationService.getAchievementTitle(
                saved,
                Locale.forLanguageTag("en")
        );

        // Then - Should fallback to achievement code (no localization exists)
        assertThat(result).isEqualTo("TEST_ACHIEVEMENT");
    }

    @Test
    void testGetAchievementDescriptionFallbackToEmpty() {
        // Given
        AchievementDefinition testAchievement = AchievementDefinition.builder()
                .code("TEST_ACHIEVEMENT_2")
                .category(AchievementCategory.REFERRAL)
                .emoji("🎯")
                .targetValue(50)
                .rewardTon(5)
                .isActive(true)
                .isLegendary(false)
                .sortOrder(1000)
                .build();

        // When
        AchievementDefinition saved = achievementDefinitionRepository.saveAndFlush(testAchievement);
        String result = entityLocalizationService.getAchievementDescription(
                saved,
                Locale.forLanguageTag("en")
        );

        // Then - Should fallback to empty string (no localization exists)
        assertThat(result).isEmpty();
    }

    @Test
    void testLocalizationConsistencyAcrossEntities() {
        // Given
        Locale ruLocale = Locale.forLanguageTag("ru");

        // When - Get localized names for different entity types
        ExerciseType pushUp = exerciseTypeRepository.findByCode("push_up").orElse(null);
        Rank seed = findRankByCodeOrSkip("seed");
        AchievementDefinition streak10 = findAchievementByCodeOrSkip("STREAK_10_DAYS");

        // Then - All should return Russian translations
        if (pushUp != null) {
            String exerciseTitle = entityLocalizationService.getExerciseTypeTitle(pushUp, ruLocale);
            assertThat(exerciseTitle).isEqualTo("Отжимания");
        }

        if (seed != null) {
            String rankTitle = entityLocalizationService.getRankTitle(seed, ruLocale);
            assertThat(rankTitle).isEqualTo("Зерно");
        }

        if (streak10 != null) {
            String achievementTitle = entityLocalizationService.getAchievementTitle(streak10, ruLocale);
            assertThat(achievementTitle).isNotEmpty();
        }
    }

    /**
     * Helper method to find rank by code or skip test if not found
     */
    private Rank findRankByCodeOrSkip(String code) {
        List<Rank> allRanks = rankRepository.findAll();
        return allRanks.stream()
                .filter(r -> code.equals(r.getCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Helper method to find achievement by code or skip test if not found
     */
    private AchievementDefinition findAchievementByCodeOrSkip(String code) {
        return achievementDefinitionRepository.findByCode(code).orElse(null);
    }
}
