package com.github.sportbot.repository;

import com.github.sportbot.model.Sex;
import com.github.sportbot.model.User;
import com.github.sportbot.model.WeightHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WeightHistoryRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private WeightHistoryRepository weightHistoryRepository;

    @Test
    void testFindByUserTelegramIdOrderByDateDesc() {
        // Given
        User user = createAndPersistUser("Test User", 1000001L);

        WeightHistory weight1 = createWeightEntry(user, 100.0f, LocalDate.now().minusDays(10));
        WeightHistory weight2 = createWeightEntry(user, 98.0f, LocalDate.now().minusDays(5));
        WeightHistory weight3 = createWeightEntry(user, 96.0f, LocalDate.now());

        entityManager.persist(weight1);
        entityManager.persist(weight2);
        entityManager.persist(weight3);
        entityManager.flush();

        // When
        List<WeightHistory> weights = weightHistoryRepository.findByUserTelegramIdOrderByDateDesc(user.getTelegramId());

        // Then
        assertEquals(3, weights.size());
        assertEquals(96.0f, weights.get(0).getWeight(), 0.01f);  // most recent first
        assertEquals(98.0f, weights.get(1).getWeight(), 0.01f);
        assertEquals(100.0f, weights.get(2).getWeight(), 0.01f);  // oldest last
    }

    @Test
    void testFindFirstByUserTelegramIdOrderByDateDesc() {
        // Given
        User user = createAndPersistUser("Test User", 1000002L);

        WeightHistory weight1 = createWeightEntry(user, 100.0f, LocalDate.now().minusDays(10));
        WeightHistory weight2 = createWeightEntry(user, 98.0f, LocalDate.now().minusDays(5));
        WeightHistory weight3 = createWeightEntry(user, 96.0f, LocalDate.now());

        entityManager.persist(weight1);
        entityManager.persist(weight2);
        entityManager.persist(weight3);
        entityManager.flush();

        // When
        Optional<WeightHistory> latestWeight = weightHistoryRepository.findFirstByUserTelegramIdOrderByDateDesc(user.getTelegramId());

        // Then
        assertTrue(latestWeight.isPresent());
        assertEquals(96.0f, latestWeight.get().getWeight(), 0.01f);  // most recent
        assertEquals(LocalDate.now(), latestWeight.get().getDate());
    }

    @Test
    void testFindByUserTelegramIdAndDateBetween() {
        // Given
        User user = createAndPersistUser("Test User", 1000003L);
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        WeightHistory weight1 = createWeightEntry(user, 100.0f, startDate.minusDays(1));  // before range
        WeightHistory weight2 = createWeightEntry(user, 99.0f, startDate);
        WeightHistory weight3 = createWeightEntry(user, 98.0f, startDate.plusDays(3));
        WeightHistory weight4 = createWeightEntry(user, 97.0f, endDate);
        WeightHistory weight5 = createWeightEntry(user, 96.0f, endDate.plusDays(1));  // after range

        entityManager.persist(weight1);
        entityManager.persist(weight2);
        entityManager.persist(weight3);
        entityManager.persist(weight4);
        entityManager.persist(weight5);
        entityManager.flush();

        // When
        List<WeightHistory> weights = weightHistoryRepository.findByUserTelegramIdAndDateBetween(
                user.getTelegramId(), startDate, endDate);

        // Then
        assertEquals(3, weights.size());  // only weight2, weight3, weight4
        assertTrue(weights.stream().anyMatch(w -> w.getWeight() == 99.0f));
        assertTrue(weights.stream().anyMatch(w -> w.getWeight() == 98.0f));
        assertTrue(weights.stream().anyMatch(w -> w.getWeight() == 97.0f));
        assertFalse(weights.stream().anyMatch(w -> w.getWeight() == 100.0f));  // before range
        assertFalse(weights.stream().anyMatch(w -> w.getWeight() == 96.0f));   // after range
    }

    @Test
    void testFindFirstByUserTelegramIdOrderByDateDesc_NoEntries() {
        // Given
        User user = createAndPersistUser("Test User", 1000004L);
        // No weight entries

        // When
        Optional<WeightHistory> latestWeight = weightHistoryRepository.findFirstByUserTelegramIdOrderByDateDesc(user.getTelegramId());

        // Then
        assertFalse(latestWeight.isPresent());
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

    private WeightHistory createWeightEntry(User user, Float weight, LocalDate date) {
        return WeightHistory.builder()
                .user(user)
                .weight(weight)
                .date(date)
                .build();
    }
}
