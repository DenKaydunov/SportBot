package com.github.sportbot.repository;

import com.github.sportbot.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional
class MealEntryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MealEntryRepository mealEntryRepository;

    @Test
    void testSumCaloriesByUserAndDate() {
        // Given
        User user = createAndPersistUser("Test User", 9000001L);
        LocalDate today = LocalDate.now();

        MealEntry meal1 = createMealEntry(user, "Breakfast", 300.0f, 20.0f, 30.0f, 10.0f, today);
        MealEntry meal2 = createMealEntry(user, "Lunch", 500.0f, 40.0f, 50.0f, 20.0f, today);
        MealEntry meal3 = createMealEntry(user, "Yesterday", 400.0f, 30.0f, 40.0f, 15.0f, today.minusDays(1));

        entityManager.persist(meal1);
        entityManager.persist(meal2);
        entityManager.persist(meal3);
        entityManager.flush();

        // When
        Float totalCalories = mealEntryRepository.sumCaloriesByUserAndDate(user, today);

        // Then
        assertEquals(800.0f, totalCalories, 0.01f);  // 300 + 500, not including yesterday
    }

    @Test
    void testSumMacrosByUserAndDate() {
        // Given
        User user = createAndPersistUser("Test User", 9000002L);
        LocalDate today = LocalDate.now();

        MealEntry meal1 = createMealEntry(user, "Meal1", 300.0f, 20.0f, 30.0f, 10.0f, today);
        MealEntry meal2 = createMealEntry(user, "Meal2", 500.0f, 40.0f, 50.0f, 20.0f, today);

        entityManager.persist(meal1);
        entityManager.persist(meal2);
        entityManager.flush();

        // When
        MacroProjection macros = mealEntryRepository.sumMacrosByUserAndDate(user, today);

        // Then
        assertNotNull(macros);
        assertEquals(60.0f, macros.getProtein(), 0.01f);  // 20 + 40
        assertEquals(80.0f, macros.getCarbs(), 0.01f);    // 30 + 50
        assertEquals(30.0f, macros.getFat(), 0.01f);      // 10 + 20
    }

    @Test
    void testFindByUserTelegramIdAndDate() {
        // Given
        User user = createAndPersistUser("Test User", 9000003L);
        LocalDate today = LocalDate.now();

        MealEntry meal1 = createMealEntry(user, "Breakfast", 300.0f, 20.0f, 30.0f, 10.0f, today);
        MealEntry meal2 = createMealEntry(user, "Lunch", 500.0f, 40.0f, 50.0f, 20.0f, today);
        MealEntry meal3 = createMealEntry(user, "Yesterday", 400.0f, 30.0f, 40.0f, 15.0f, today.minusDays(1));

        entityManager.persist(meal1);
        entityManager.persist(meal2);
        entityManager.persist(meal3);
        entityManager.flush();

        // When
        List<MealEntry> meals = mealEntryRepository.findByUserTelegramIdAndDate(user.getTelegramId(), today);

        // Then
        assertEquals(2, meals.size());
        assertTrue(meals.stream().anyMatch(m -> m.getFoodName().equals("Breakfast")));
        assertTrue(meals.stream().anyMatch(m -> m.getFoodName().equals("Lunch")));
    }

    @Test
    void testFindByUserTelegramIdAndPeriod() {
        // Given
        User user = createAndPersistUser("Test User", 9000004L);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        MealEntry meal1 = createMealEntry(user, "Day1", 300.0f, 20.0f, 30.0f, 10.0f, startDate);
        MealEntry meal2 = createMealEntry(user, "Day3", 500.0f, 40.0f, 50.0f, 20.0f, startDate.plusDays(2));
        MealEntry meal3 = createMealEntry(user, "Day10", 400.0f, 30.0f, 40.0f, 15.0f, startDate.plusDays(9)); // outside period

        entityManager.persist(meal1);
        entityManager.persist(meal2);
        entityManager.persist(meal3);
        entityManager.flush();

        // When
        List<MealEntry> meals = mealEntryRepository.findByUserTelegramIdAndPeriod(
                user.getTelegramId(), startDate, endDate);

        // Then
        assertEquals(2, meals.size());  // day10 is outside period
        assertTrue(meals.stream().anyMatch(m -> m.getFoodName().equals("Day1")));
        assertTrue(meals.stream().anyMatch(m -> m.getFoodName().equals("Day3")));
    }

    @Test
    void testCountDistinctMealDaysByUser() {
        // Given
        User user = createAndPersistUser("Test User", 9000005L);
        LocalDate today = LocalDate.now();
        LocalDate thirtyDaysAgo = today.minusDays(30);

        // Log multiple meals on same days
        entityManager.persist(createMealEntry(user, "Meal1", 300.0f, 20.0f, 30.0f, 10.0f, today));
        entityManager.persist(createMealEntry(user, "Meal2", 300.0f, 20.0f, 30.0f, 10.0f, today));  // same day
        entityManager.persist(createMealEntry(user, "Meal3", 300.0f, 20.0f, 30.0f, 10.0f, today.minusDays(1)));
        entityManager.persist(createMealEntry(user, "Meal4", 300.0f, 20.0f, 30.0f, 10.0f, today.minusDays(2)));
        entityManager.persist(createMealEntry(user, "Meal5", 300.0f, 20.0f, 30.0f, 10.0f, today.minusDays(35)));  // too old
        entityManager.flush();

        // When
        Long distinctDays = mealEntryRepository.countDistinctMealDaysByUser(user, thirtyDaysAgo);

        // Then
        assertEquals(3L, distinctDays);  // today, -1 day, -2 days (not -35 days)
    }

    @Test
    void testSumCaloriesByUserAndDate_NoMeals() {
        // Given
        User user = createAndPersistUser("Test User", 9000006L);
        LocalDate today = LocalDate.now();

        // When
        Float totalCalories = mealEntryRepository.sumCaloriesByUserAndDate(user, today);

        // Then
        assertEquals(0.0f, totalCalories, 0.01f);  // COALESCE returns 0
    }

    private User createAndPersistUser(String name, Long telegramId) {
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .fullName(name)
                .telegramId(telegramId)
                .age(30)
                .sex(Sex.MAN)
                .language("ru")
                .isSubscribed(true)
                .currentStreak(0)
                .bestStreak(0)
                .balanceTon(0)
                .build();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        entityManager.persist(user);
        entityManager.flush();
        return user;
    }

    private MealEntry createMealEntry(User user, String foodName, Float calories,
                                     Float protein, Float carbs, Float fat, LocalDate date) {
        MealEntry entry = MealEntry.builder()
                .user(user)
                .foodName(foodName)
                .calories(calories)
                .protein(protein)
                .carbs(carbs)
                .fat(fat)
                .date(date)
                .build();
        entry.setCreatedAt(LocalDateTime.now());
        return entry;
    }
}
