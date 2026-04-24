package com.github.sportbot.service.achievement;

import com.github.sportbot.dto.UserExercisePosition;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.CompetitorsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaderboardAchievementCheckerTest {

    @Mock
    private CompetitorsRepository competitorsRepository;

    @InjectMocks
    private LeaderboardAchievementChecker checker;

    private User user;
    private AchievementDefinition dominatorDefinition;
    private AchievementDefinition top10Definition;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);

        dominatorDefinition = new AchievementDefinition();
        dominatorDefinition.setCode("LEADERBOARD_DOMINATOR");
        dominatorDefinition.setTargetValue(4);

        top10Definition = new AchievementDefinition();
        top10Definition.setCode("LEADERBOARD_TOP_10");
        top10Definition.setTargetValue(10);
    }

    @Test
    void testCalculateProgress_Dominator_FourFirstPlaces() {
        // User is #1 in all four exercises
        List<UserExercisePosition> positions = Arrays.asList(
                new UserExercisePosition(1L, 1, 1000L), // push-ups: #1
                new UserExercisePosition(2L, 1, 500L),  // pull-ups: #1
                new UserExercisePosition(3L, 1, 2000L), // squats: #1
                new UserExercisePosition(4L, 1, 300L)   // abs: #1
        );

        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(positions);

        int progress = checker.calculateProgress(user, dominatorDefinition);

        assertEquals(1, progress);
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }

    @Test
    void testCalculateProgress_Dominator_ThreeFirstPlaces() {
        // User is #1 in only 3 exercises (not enough for DOMINATOR)
        List<UserExercisePosition> positions = Arrays.asList(
                new UserExercisePosition(1L, 1, 1000L),  // push-ups: #1
                new UserExercisePosition(2L, 1, 500L),   // pull-ups: #1
                new UserExercisePosition(3L, 1, 2000L),  // squats: #1
                new UserExercisePosition(4L, 5, 300L)    // abs: #5
        );

        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(positions);

        int progress = checker.calculateProgress(user, dominatorDefinition);

        assertEquals(0, progress);
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }

    @Test
    void testCalculateProgress_Top10_UserInTop10() {
        // User is #5 in best exercise
        List<UserExercisePosition> positions = Arrays.asList(
                new UserExercisePosition(1L, 5, 1000L),
                new UserExercisePosition(2L, 15, 500L),
                new UserExercisePosition(3L, 20, 2000L)
        );

        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(positions);

        int progress = checker.calculateProgress(user, top10Definition);

        assertEquals(1, progress);
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }

    @Test
    void testCalculateProgress_Top10_UserOutsideTop10() {
        // User is #15 in best exercise
        List<UserExercisePosition> positions = Arrays.asList(
                new UserExercisePosition(1L, 15, 1000L),
                new UserExercisePosition(2L, 20, 500L)
        );

        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(positions);

        int progress = checker.calculateProgress(user, top10Definition);

        assertEquals(0, progress);
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }

    @Test
    void testCalculateProgress_NoExerciseRecords() {
        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(Collections.emptyList());

        int progress = checker.calculateProgress(user, top10Definition);

        assertEquals(0, progress);
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }

    @Test
    void testCalculateProgress_NullUser() {
        int progress = checker.calculateProgress(null, top10Definition);

        assertEquals(0, progress);
        verify(competitorsRepository, never()).findAllUserPositions(anyInt());
    }

    @Test
    void testCalculateProgress_NullDefinitionCode() {
        AchievementDefinition nullCodeDefinition = new AchievementDefinition();
        nullCodeDefinition.setCode(null);

        int progress = checker.calculateProgress(user, nullCodeDefinition);

        assertEquals(0, progress);
        verify(competitorsRepository, never()).findAllUserPositions(anyInt());
    }

    @Test
    void testGetCategory() {
        assertEquals(AchievementCategory.LEADERBOARD, checker.getCategory());
    }

    @Test
    void testCalculateProgress_SingleQuery_AvoidN1Problem() {
        // This test verifies that only ONE query is made regardless of number of exercises
        List<UserExercisePosition> positions = Arrays.asList(
                new UserExercisePosition(1L, 1, 1000L),
                new UserExercisePosition(2L, 1, 500L),
                new UserExercisePosition(3L, 1, 2000L),
                new UserExercisePosition(4L, 1, 300L)
        );

        when(competitorsRepository.findAllUserPositions(user.getId())).thenReturn(positions);

        // Check triumphator achievement
        checker.calculateProgress(user, dominatorDefinition);

        // Verify only ONE repository call was made (not 4)
        verify(competitorsRepository, times(1)).findAllUserPositions(user.getId());
    }
}
