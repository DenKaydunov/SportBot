package com.github.sportbot.service;

import com.github.sportbot.config.WorkoutProperties;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserRank;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.RankRepository;
import com.github.sportbot.repository.UserRankRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RankServiceTest {

    @Mock
    private RankRepository rankRepository;
    @Mock
    private UserRankRepository userRankRepository;

    private MessageSource messageSource;
    @Mock
    private UserService userService;
    @Mock
    private EntityLocalizationService entityLocalizationService;
    @Mock
    private WorkoutProperties workoutProperties;
    @Mock
    private ExerciseRecordRepository exerciseRecordRepository;
    @Mock
    private ExerciseTypeService exerciseTypeService;

    @InjectMocks
    private RankService rankService;

    private User user;

    @BeforeEach
    void setUp() {
        // use real messages bundle
        ResourceBundleMessageSource realMessageSource = new ResourceBundleMessageSource();
        realMessageSource.setBasename("messages");
        realMessageSource.setDefaultEncoding("UTF-8");
        this.messageSource = realMessageSource;

        // re-inject service with real message source
        this.rankService = new RankService(rankRepository, userRankRepository, messageSource, userService, entityLocalizationService, workoutProperties, exerciseRecordRepository, exerciseTypeService);

        user = User.builder().id(10).telegramId(1000L).language("ru").build();

        // Setup common mocks
        lenient().when(userService.getUserLocale(any(User.class))).thenReturn(Locale.forLanguageTag("ru"));
        lenient().when(entityLocalizationService.getRankTitle(any(com.github.sportbot.model.Rank.class), any(Locale.class)))
                .thenAnswer(inv -> ((com.github.sportbot.model.Rank) inv.getArgument(0)).getTitle());

        // Mock coefficients
        lenient().when(workoutProperties.getCoefficient("pull_up")).thenReturn(1.0);
        lenient().when(workoutProperties.getCoefficient("push_up")).thenReturn(0.43);
        lenient().when(workoutProperties.getCoefficient("squat")).thenReturn(0.31);
        lenient().when(workoutProperties.getCoefficient("abs")).thenReturn(0.27);
    }

    @Test
    void assignRankIfEligible_Promotion_NoPreviousRank_UsesDashAndSaves() {
        // Given: user has 100 XP total
        setupMockExerciseTypes();
        lenient().when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(100, 0, 0, 0); // 100 pull-ups, rest = 0

        // Global ranks exist
        when(rankRepository.existsByExerciseTypeIsNull()).thenReturn(true);

        // Achieved rank for 100 XP
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getTitle()).thenReturn("Тряпка");
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(100))
                .thenReturn(Optional.of(achievedRank));

        // User has no previous rank
        when(userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user))
                .thenReturn(Optional.empty());
        // Achieved rank not assigned yet
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false);

        // When
        String msg = rankService.assignRankIfEligible(user);

        // Then
        String expected = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"—", "Тряпка"},
                Locale.forLanguageTag("ru")
        );
        assertEquals(expected, msg);
        verify(userRankRepository).save(any(UserRank.class));
    }

    @Test
    void assignRankIfEligible_NoPromotion_ReturnsNextRankHint() {
        // Given: user has 120 XP
        setupMockExerciseTypes();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(120, 0, 0, 0);

        when(rankRepository.existsByExerciseTypeIsNull()).thenReturn(true);

        // Achieved rank for 120 XP -> "Тряпка" (threshold 100)
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(100);
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(120))
                .thenReturn(Optional.of(achievedRank));

        // User already has same rank
        var userRank = mock(UserRank.class);
        when(userRank.getRank()).thenReturn(achievedRank);
        when(userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user))
                .thenReturn(Optional.of(userRank));
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(true);

        // Next rank at threshold 250 -> need 130 XP remaining
        var nextRank = mock(com.github.sportbot.model.Rank.class);
        when(nextRank.getThreshold()).thenReturn(250);
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdGreaterThanOrderByThresholdAsc(100))
                .thenReturn(Optional.of(nextRank));

        // When
        String msg = rankService.assignRankIfEligible(user);

        // Then
        String expected = messageSource.getMessage(
                "workout.rank_next_left",
                new Object[]{130},
                Locale.forLanguageTag("ru")
        );
        assertEquals(expected, msg);
        verify(userRankRepository, never()).save(any());
    }

    @Test
    void assignRankIfEligible_NoRanksConfigured_ReturnsEmpty() {
        setupMockExerciseTypes();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(100, 0, 0, 0);

        when(rankRepository.existsByExerciseTypeIsNull()).thenReturn(false);

        String msg = rankService.assignRankIfEligible(user);

        assertEquals("", msg);
        verifyNoInteractions(userRankRepository);
        verify(rankRepository, never())
                .findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(anyInt());
    }

    @Test
    void assignRankIfEligible_ExactThreshold_PromotesFromLowerRank() {
        // Given: user has exactly 100 XP
        setupMockExerciseTypes();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(100, 0, 0, 0);

        when(rankRepository.existsByExerciseTypeIsNull()).thenReturn(true);

        // Achieved rank exactly at threshold 100
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(100);
        when(achievedRank.getTitle()).thenReturn("Тряпка");
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(100))
                .thenReturn(Optional.of(achievedRank));

        // User currently has lower rank (threshold 50, title "Червь")
        var lowerRank = mock(com.github.sportbot.model.Rank.class);
        when(lowerRank.getThreshold()).thenReturn(50);
        when(lowerRank.getTitle()).thenReturn("Червь");
        var currentUserRank = mock(UserRank.class);
        when(currentUserRank.getRank()).thenReturn(lowerRank);
        when(userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user))
                .thenReturn(Optional.of(currentUserRank));

        // Achieved rank not assigned yet -> should promote
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false);

        // When
        String msg = rankService.assignRankIfEligible(user);

        // Then
        String expected = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"Червь", "Тряпка"},
                Locale.forLanguageTag("ru")
        );

        assertEquals(expected, msg);
        verify(userRankRepository).save(any(UserRank.class));
    }

    @Test
    void assignRankIfEligible_Idempotent_SecondCallDoesNotSaveAgain() {
        // Given: user has 100 XP (both calls)
        setupMockExerciseTypes();
        lenient().when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(100, 0, 0, 0, 100, 0, 0, 0); // Two calls to calculateTotalXP

        when(rankRepository.existsByExerciseTypeIsNull()).thenReturn(true);

        // Achieved rank at 100
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(100);
        when(achievedRank.getTitle()).thenReturn("Тряпка");
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(100))
                .thenReturn(Optional.of(achievedRank));

        // User had no previous rank on first call, then has rank on second call
        var userRank = mock(UserRank.class);
        when(userRank.getRank()).thenReturn(achievedRank);
        when(userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user))
                .thenReturn(Optional.empty(), Optional.of(userRank));

        // First call: not assigned yet -> false, second call: already assigned -> true
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false, true);

        // Next rank at 250
        var nextRank = mock(com.github.sportbot.model.Rank.class);
        when(nextRank.getThreshold()).thenReturn(250);
        when(rankRepository.findTopByExerciseTypeIsNullAndThresholdGreaterThanOrderByThresholdAsc(100))
                .thenReturn(Optional.of(nextRank));

        // First call -> promotion message
        String first = rankService.assignRankIfEligible(user);
        String expectedFirst = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"—", "Тряпка"},
                Locale.forLanguageTag("ru")
        );
        assertEquals(expectedFirst, first);

        // Second call -> no promotion, has next rank hint
        String second = rankService.assignRankIfEligible(user);
        String expectedSecond = messageSource.getMessage(
                "workout.rank_next_left",
                new Object[]{150},
                Locale.forLanguageTag("ru")
        );
        assertEquals(expectedSecond, second);

        // Save should be performed exactly once
        verify(userRankRepository, times(1)).save(any(UserRank.class));
    }

    @Test
    void calculateTotalXP_MixedExercises_ReturnsCorrectSum() {
        // Given: user has various exercise totals
        setupMockExerciseTypes();
        when(exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(eq(user), any(ExerciseType.class)))
                .thenReturn(50, 100, 100, 100); // 50 pull-ups, 100 push-ups, 100 squats, 100 abs

        // When
        double xp = rankService.calculateTotalXP(user);

        // Then: 50*1.0 + 100*0.43 + 100*0.31 + 100*0.27 = 50 + 43 + 31 + 27 = 151
        assertEquals(151.0, xp, 0.01);
    }

    private void setupMockExerciseTypes() {
        ExerciseType pullUp = ExerciseType.builder().id(2L).code("pull_up").build();
        ExerciseType pushUp = ExerciseType.builder().id(1L).code("push_up").build();
        ExerciseType squat = ExerciseType.builder().id(3L).code("squat").build();
        ExerciseType abs = ExerciseType.builder().id(4L).code("abs").build();

        when(exerciseTypeService.getExerciseType("pull_up")).thenReturn(pullUp);
        when(exerciseTypeService.getExerciseType("push_up")).thenReturn(pushUp);
        when(exerciseTypeService.getExerciseType("squat")).thenReturn(squat);
        when(exerciseTypeService.getExerciseType("abs")).thenReturn(abs);
    }
}
