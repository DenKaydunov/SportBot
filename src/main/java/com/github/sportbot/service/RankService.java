package com.github.sportbot.service;

import com.github.sportbot.exception.RankNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserRank;
import com.github.sportbot.repository.RankRepository;
import com.github.sportbot.repository.UserRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;
    private final UserRankRepository userRankRepository;
    private final MessageSource messageSource;

    /**
     * Assigns a rank to the user for the given exercise type if the total reps meet a threshold
     * and this rank is higher than the user's current rank for that exercise type.
     * The method is idempotent and will not duplicate assignments.
     *
     * Note: If there are no ranks configured for the provided exercise type, this method is a no-op
     * and returns an empty message.
     *
     * @return a localized promotion/next-rank message or empty string when nothing to report.
     */
    @Transactional
    public String assignRankIfEligible(User user, ExerciseType exerciseType, int totalReps) {
        String message = "";
        if (!hasRanksConfigured(exerciseType)) {
            return message;
        }

        Rank currentRank = getCurrentRank(exerciseType, totalReps);
        Optional<UserRank> userRank = getUserRank(user, exerciseType);

        if (isPromotion(user, userRank, currentRank)) {
            saveUserRank(user, currentRank);
            message = buildPromotionMessage(userRank, currentRank);
        } else {
            message = buildNextRankHint(exerciseType, currentRank, totalReps);
        }
        return message;
    }

    private Rank getCurrentRank(ExerciseType exerciseType, int totalReps) {
        return rankRepository.findTopByExerciseTypeAndThresholdLessThanEqualOrderByThresholdDesc(exerciseType, totalReps)
                .orElseThrow(() -> new RankNotFoundException(exerciseType.getCode(), totalReps)
                );
    }
    
    private Optional<UserRank> getUserRank(User user, ExerciseType exerciseType) {
        return userRankRepository
                .findTopByUserAndRank_ExerciseTypeOrderByRank_ThresholdDesc(user, exerciseType);
    }
    
    private boolean hasRanksConfigured(ExerciseType exerciseType) {
        return rankRepository.existsByExerciseType(exerciseType);
    }

    private String buildPromotionMessage(Optional<UserRank> currentForType, Rank achievedRank) {
        Object previousTitle = currentForType.map(ur -> ur.getRank().getTitle()).orElse("—");
        return messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{previousTitle, achievedRank.getTitle()},
                Locale.forLanguageTag("ru-RU")
        );
    }

    private String buildNextRankHint(ExerciseType exerciseType, Rank achievedRank, int totalReps) {
        return rankRepository
                .findTopByExerciseTypeAndThresholdGreaterThanOrderByThresholdAsc(exerciseType, achievedRank.getThreshold())
                .map(next -> {
                    int remaining = Math.max(0, next.getThreshold() - totalReps);
                    if (remaining > 0) {
                        return messageSource.getMessage(
                                "workout.rank_next_left",
                                new Object[]{remaining},
                                Locale.forLanguageTag("ru-RU")
                        );
                    }
                    return "";
                })
                .orElse("");
    }

    /**
     * Checks whether rank assignment is required:
     * 1. Compares the achieved rank with the user's current highest rank for the exercise type.
     * 2. Ensures the achieved rank hasn't already been assigned to the user (idempotency).
     *
     * @return true if the user should be promoted (new, higher and not yet assigned rank), false otherwise.
     */
    private boolean isPromotion(User user, Optional<UserRank> currentForType, Rank achievedRank) {
        boolean isHigherThanCurrent = currentForType
                .map(ur -> ur.getRank().getThreshold() < achievedRank.getThreshold())
                .orElse(true);
        boolean notAssignedYet = !userRankRepository.existsByUserAndRank(user, achievedRank);
        return isHigherThanCurrent && notAssignedYet;
    }

    /**
     * Creates and saves a new UserRank record.
     */
    private void saveUserRank(User user, Rank rank) {
        UserRank userRank = UserRank.builder()
                .user(user)
                .rank(rank)
                .assignedAt(LocalDateTime.now())
                .build();
        userRankRepository.save(userRank);
    }

    /**
     * Returns the title of the highest rank of the user or the provided defaultTitle
     * when the user has no ranks yet.
     */
    public String getRankTitle(User user) {
        return userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user)
                .map(ur -> ur.getRank().getTitle())
                .orElse("-");
    }

}