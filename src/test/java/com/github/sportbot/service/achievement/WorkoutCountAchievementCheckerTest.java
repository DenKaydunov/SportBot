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
class WorkoutCountAchievementCheckerTest {

    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;

    @InjectMocks
    private WorkoutCountAchievementChecker checker;

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
        definition.setCategory(AchievementCategory.WORKOUT_COUNT);
    }

    @Test
    void getCategory_shouldReturnWorkoutCount() {
        // When
        AchievementCategory category = checker.getCategory();

        // Then
        assertThat(category).isEqualTo(AchievementCategory.WORKOUT_COUNT);
    }

    @Test
    void calculateProgress_withSpecificExerciseType_shouldReturnWorkoutDays() {
        // Given
        definition.setExerciseType(exerciseType);
        when(exerciseRecordRepository.countDistinctWorkoutDaysByUserAndExerciseType(testUser, exerciseType))
                .thenReturn(15L);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(15);
    }

    @Test
    void calculateProgress_withoutExerciseType_shouldReturnAllWorkoutDays() {
        // Given
        definition.setExerciseType(null);
        when(exerciseRecordRepository.countDistinctWorkoutDaysByUser(testUser))
                .thenReturn(30L);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(30);
    }

    @Test
    void calculateProgress_whenNoWorkouts_shouldReturnZero() {
        // Given
        definition.setExerciseType(exerciseType);
        when(exerciseRecordRepository.countDistinctWorkoutDaysByUserAndExerciseType(testUser, exerciseType))
                .thenReturn(0L);

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
    void calculateProgress_whenRepositoryReturnsNull_shouldReturnZero() {
        // Given
        definition.setExerciseType(null);
        when(exerciseRecordRepository.countDistinctWorkoutDaysByUser(testUser))
                .thenReturn(null);

        // When
        int progress = checker.calculateProgress(testUser, definition);

        // Then
        assertThat(progress).isEqualTo(0);
    }
}
