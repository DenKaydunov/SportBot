package com.github.sportbot.repository;

import com.github.sportbot.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class LeaderBoardRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LeaderBoardRepository leaderBoardRepository;

    private User user;
    private ExerciseType exerciseType;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        // Clear existing data to have a clean state for each test
        entityManager.getEntityManager().createQuery("DELETE FROM UserTag").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM ExerciseRecord").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();

        // Use existing exercise type if it exists, or create new one
        exerciseType = entityManager.getEntityManager()
                .createQuery("SELECT et FROM ExerciseType et WHERE et.code = :code", ExerciseType.class)
                .setParameter("code", "squat")
                .getResultList()
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    ExerciseType newEt = ExerciseType.builder()
                            .code("squat")
                            .title("Приседания")
                            .build();
                    return entityManager.persist(newEt);
                });

        user = User.builder()
                .fullName("Test User")
                .telegramId(12345L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isSubscribed(true)
                .build();
        user = entityManager.persist(user);

        tag1 = Tag.builder()
                .code("TAG1")
                .title("Tag 1")
                .createdAt(LocalDateTime.now())
                .build();
        tag1 = entityManager.persist(tag1);

        tag2 = Tag.builder()
                .code("TAG2")
                .title("Tag 2")
                .createdAt(LocalDateTime.now())
                .build();
        tag2 = entityManager.persist(tag2);

        // Assign both tags to the user
        entityManager.persist(UserTag.builder()
                .user(user)
                .challengeTag(tag1)
                .assignedAt(LocalDateTime.now())
                .build());
        entityManager.persist(UserTag.builder()
                .user(user)
                .challengeTag(tag2)
                .assignedAt(LocalDateTime.now())
                .build());

        // Add an exercise record
        entityManager.persist(ExerciseRecord.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(50)
                .date(LocalDate.now())
                .build());

        entityManager.flush();
    }

    @Test
    void sumCountByExerciseTypeAndDate_ShouldNotDoubleCountWhenUserHasMultipleTags() {
        // When
        int total = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), null, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        // Then
        // Before the fix, this would be 100 because of the LEFT JOIN with user_tags (2 tags * 50 reps)
        assertThat(total).isEqualTo(50);
    }

    @Test
    void findTopUsersByExerciseTypeAndDate_ShouldNotReturnDuplicateRowsWhenUserHasMultipleTags() {
        // When
        List<Object[]> results = leaderBoardRepository.findTopUsersByExerciseTypeAndDate(
                exerciseType.getId(), null, 10, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)[0]).isEqualTo("Test User");
        assertThat(results.get(0)[1]).isEqualTo(50L);
    }

    @Test
    void shouldFilterByTagCorrectly() {
        // When
        int totalWithTag1 = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), tag1.getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        // Then
        assertThat(totalWithTag1).isEqualTo(50);

        // When
        // Add another user with different tag
        User user2 = User.builder()
                .fullName("User 2")
                .telegramId(67890L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isSubscribed(true)
                .build();
        user2 = entityManager.persist(user2);

        entityManager.persist(UserTag.builder()
                .user(user2)
                .challengeTag(tag2)
                .assignedAt(LocalDateTime.now())
                .build());

        entityManager.persist(ExerciseRecord.builder()
                .user(user2)
                .exerciseType(exerciseType)
                .count(30)
                .date(LocalDate.now())
                .build());
        entityManager.flush();

        int totalWithTag1Again = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), tag1.getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(totalWithTag1Again).isEqualTo(50);

        int totalWithTag2 = leaderBoardRepository.sumCountByExerciseTypeAndDate(
                exerciseType.getId(), tag2.getId(), LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        assertThat(totalWithTag2).isEqualTo(80); // 50 from user1 + 30 from user2
    }

    @Test
    void findTopUsersByExerciseTypeAndDatePaged_ShouldFailWithInvalidSort() {
        // Given
        org.springframework.data.domain.Pageable pageableWithInvalidSort =
                org.springframework.data.domain.PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("string"));

        // When & Then
        // This is expected to throw an exception because 'string' column doesn't exist
        org.junit.jupiter.api.Assertions.assertThrows(org.springframework.dao.DataAccessException.class,
                () -> leaderBoardRepository.findTopUsersByExerciseTypeAndDatePaged(
                        exerciseType.getId(), null, LocalDate.now(), LocalDate.now(), pageableWithInvalidSort));
    }
}
