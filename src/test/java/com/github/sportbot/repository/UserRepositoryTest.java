package com.github.sportbot.repository;

import com.github.sportbot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User referrer;
    private User referral1;
    private User referral2;
    private User referral3;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        // Create referrer
        referrer = User.builder()
                .telegramId(123456L)
                .fullName("Referrer User")
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(referrer);

        // Create referrals
        referral1 = User.builder()
                .telegramId(111111L)
                .fullName("Referral 1")
                .referrerTelegramId(referrer.getTelegramId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(referral1);

        referral2 = User.builder()
                .telegramId(222222L)
                .fullName("Referral 2")
                .referrerTelegramId(referrer.getTelegramId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(referral2);

        referral3 = User.builder()
                .telegramId(333333L)
                .fullName("Referral 3")
                .referrerTelegramId(referrer.getTelegramId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(referral3);

        // Create user without referrer
        User userWithoutReferrer = User.builder()
                .telegramId(444444L)
                .fullName("Independent User")
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(userWithoutReferrer);

        entityManager.flush();
    }

    @Test
    void findAllByReferrerTelegramId_ReturnsAllReferrals() {
        // When
        List<User> referrals = userRepository.findAllByReferrerTelegramId(referrer.getTelegramId());

        // Then
        assertNotNull(referrals);
        assertEquals(3, referrals.size());
        assertTrue(referrals.stream().allMatch(u -> u.getReferrerTelegramId().equals(referrer.getTelegramId())));
        assertTrue(referrals.stream().anyMatch(u -> u.getFullName().equals("Referral 1")));
        assertTrue(referrals.stream().anyMatch(u -> u.getFullName().equals("Referral 2")));
        assertTrue(referrals.stream().anyMatch(u -> u.getFullName().equals("Referral 3")));
    }

    @Test
    void findAllByReferrerTelegramId_WithNoReferrals_ReturnsEmptyList() {
        // Given
        Long nonReferrerTelegramId = 999999L;

        // When
        List<User> referrals = userRepository.findAllByReferrerTelegramId(nonReferrerTelegramId);

        // Then
        assertNotNull(referrals);
        assertTrue(referrals.isEmpty());
    }

    @Test
    void countByReferrerTelegramId_ReturnsCorrectCount() {
        // When
        Integer count = userRepository.countByReferrerTelegramId(referrer.getTelegramId());

        // Then
        assertNotNull(count);
        assertEquals(3, count);
    }

    @Test
    void countByReferrerTelegramId_WithNoReferrals_ReturnsZero() {
        // Given
        Long nonReferrerTelegramId = 999999L;

        // When
        Integer count = userRepository.countByReferrerTelegramId(nonReferrerTelegramId);

        // Then
        assertNotNull(count);
        assertEquals(0, count);
    }

    @Test
    void findAllByReferrerTelegramId_AfterAddingNewReferral_ReturnsUpdatedList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User newReferral = User.builder()
                .telegramId(555555L)
                .fullName("New Referral")
                .referrerTelegramId(referrer.getTelegramId())
                .createdAt(now)
                .updatedAt(now)
                .build();
        entityManager.persist(newReferral);
        entityManager.flush();

        // When
        List<User> referrals = userRepository.findAllByReferrerTelegramId(referrer.getTelegramId());

        // Then
        assertNotNull(referrals);
        assertEquals(4, referrals.size());
        assertTrue(referrals.stream().anyMatch(u -> u.getFullName().equals("New Referral")));
    }
}
