package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserRank;
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

    @InjectMocks
    private RankService rankService;

    private ExerciseType exerciseType;
    private User user;

    @BeforeEach
    void setUp() {
        // use real messages bundle
        ResourceBundleMessageSource realMessageSource = new ResourceBundleMessageSource();
        realMessageSource.setBasename("messages");
        realMessageSource.setDefaultEncoding("UTF-8");
        this.messageSource = realMessageSource;

        // re-inject service with real message source
        this.rankService = new RankService(rankRepository, userRankRepository, messageSource);

        exerciseType = ExerciseType.builder().id(1L).code("pull_up").title("Подтягивания").build();
        user = User.builder().id(10).telegramId(1000L).build();
    }

    @Test
    void assignRankIfEligible_Promotion_NoPreviousRank_UsesDashAndSaves() {
        int total = 15;

        // ranks exist
        when(rankRepository.existsByExerciseType(exerciseType)).thenReturn(true);

        // achieved current rank for total reps
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getTitle()).thenReturn("Новичок");
        when(rankRepository.findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(exerciseType, total))
                .thenReturn(Optional.of(achievedRank));

        // user has no previous rank for this type
        when(userRankRepository.findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(user, exerciseType))
                .thenReturn(Optional.empty());
        // achieved rank not assigned yet
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false);

        String msg = rankService.assignRankIfEligible(user, exerciseType, total);

        // message should contain dash for previous rank and achieved title
        String expected = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"—", "Новичок"},
                Locale.forLanguageTag("ru-RU")
        );
        assertEquals(expected, msg);
        verify(userRankRepository).save(any(UserRank.class));
    }

    @Test
    void assignRankIfEligible_NoPromotion_ReturnsNextRankHint() {
        int total = 12;
        when(rankRepository.existsByExerciseType(exerciseType)).thenReturn(true);

        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(10);
        when(rankRepository.findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(exerciseType, total))
                .thenReturn(Optional.of(achievedRank));

        // already has same current highest rank -> no promotion
        var userRank = mock(UserRank.class);
        when(userRank.getRank()).thenReturn(achievedRank);
        when(userRankRepository.findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(user, exerciseType))
                .thenReturn(Optional.of(userRank));
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(true);

        // next rank at threshold 25 -> need 13 reps remaining
        var nextRank = mock(com.github.sportbot.model.Rank.class);
        when(nextRank.getThreshold()).thenReturn(25);
        when(rankRepository.findTopByExerciseTypeAndThresholdGreaterThanOrderByThresholdAsc(exerciseType, 10))
                .thenReturn(Optional.of(nextRank));

        String msg = rankService.assignRankIfEligible(user, exerciseType, total);
        String expected = messageSource.getMessage(
                "workout.rank_next_left",
                new Object[]{13},
                Locale.forLanguageTag("ru-RU")
        );
        assertEquals(expected, msg);
        verify(userRankRepository, never()).save(any());
    }

    @Test
    void assignRankIfEligible_NoRanksConfigured_ReturnsEmpty() {
        when(rankRepository.existsByExerciseType(exerciseType)).thenReturn(false);

        String msg = rankService.assignRankIfEligible(user, exerciseType, 100);
        assertEquals("", msg);
        verifyNoInteractions(userRankRepository);
        verify(rankRepository, never())
                .findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(any(), anyInt());
    }

    @Test
    void assignRankIfEligible_ExactThreshold_PromotesFromLowerRank() {
        int total = 100;

        when(rankRepository.existsByExerciseType(exerciseType)).thenReturn(true);

        // achieved rank exactly at threshold 100
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(100);
        when(achievedRank.getTitle()).thenReturn("Новичок");
        when(rankRepository.findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(exerciseType, total))
                .thenReturn(Optional.of(achievedRank));

        // user currently has lower rank (threshold 50, title "Стажер")
        var lowerRank = mock(com.github.sportbot.model.Rank.class);
        when(lowerRank.getThreshold()).thenReturn(50);
        when(lowerRank.getTitle()).thenReturn("Стажер");
        var currentUserRank = mock(UserRank.class);
        when(currentUserRank.getRank()).thenReturn(lowerRank);
        when(userRankRepository.findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(user, exerciseType))
                .thenReturn(Optional.of(currentUserRank));

        // achieved rank not assigned yet -> should promote
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false);

        String msg = rankService.assignRankIfEligible(user, exerciseType, total);

        String expected = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"Стажер", "Новичок"},
                Locale.forLanguageTag("ru-RU")
        );

        assertEquals(expected, msg);
        verify(userRankRepository).save(any(UserRank.class));
    }

    @Test
    void assignRankIfEligible_Idempotent_SecondCallDoesNotSaveAgain() {
        int total = 200;

        when(rankRepository.existsByExerciseType(exerciseType)).thenReturn(true);

        // achieved rank at 200
        var achievedRank = mock(com.github.sportbot.model.Rank.class);
        when(achievedRank.getThreshold()).thenReturn(200);
        when(achievedRank.getTitle()).thenReturn("Продвинутый");
        when(rankRepository.findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(exerciseType, total))
                .thenReturn(Optional.of(achievedRank));

        // user had no previous rank for type
        when(userRankRepository.findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(user, exerciseType))
                .thenReturn(Optional.empty());

        // first call: not assigned yet -> false, second call: already assigned -> true
        when(userRankRepository.existsByUserAndRank(user, achievedRank)).thenReturn(false, true);

        // next rank absent (max) -> no hint on second call
        when(rankRepository.findTopByExerciseTypeAndThresholdGreaterThanOrderByThresholdAsc(exerciseType, 200))
                .thenReturn(Optional.empty());

        // First call -> promotion message
        String first = rankService.assignRankIfEligible(user, exerciseType, total);
        String expectedFirst = messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{"—", "Продвинутый"},
                Locale.forLanguageTag("ru-RU")
        );
        assertEquals(expectedFirst, first);

        // Second call -> no promotion, no next rank -> empty message
        String second = rankService.assignRankIfEligible(user, exerciseType, total);
        assertEquals("", second);

        // Save should be performed exactly once
        verify(userRankRepository, times(1)).save(any(UserRank.class));
    }
}
