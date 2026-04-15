package com.github.sportbot.service.achievement;

import com.github.sportbot.model.AchievementCategory;
import com.github.sportbot.model.AchievementDefinition;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TotalRepsAchievementCheckerTest {

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @InjectMocks
    private TotalRepsAchievementChecker checker;

    private User testUser;
    private ExerciseType exerciseType;
    private AchievementDefinition definition;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1);

        exerciseType = new ExerciseType();
        exerciseType.setId(1L);
        exerciseType.setCode("pushups");

        definition = new AchievementDefinition();
        definition.setCategory(AchievementCategory.TOTAL_REPS);
        definition.setExerciseType(exerciseType);
    }

    @Test
    void getCategory_shouldReturnTotalReps() {
        // When
        AchievementCategory category = checker.getCategory();

        // Then
        assertThat(category).isEqualTo(AchievementCategory.TOTAL_REPS);
    }

    @Test
    void calculateProgress_whenUserHasReps_shouldReturnTotalCount() {
        // Given
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(testUser, exerciseType))
                .thenReturn(500);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(500);
    }

    @Test
    void calculateProgress_whenUserHasNoReps_shouldReturnZero() {
        // Given
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(testUser, exerciseType))
                .thenReturn(0);

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
    void calculateProgress_whenExerciseTypeIsNull_shouldReturnZero() {
        // Given
        definition.setExerciseType(null);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }
}
