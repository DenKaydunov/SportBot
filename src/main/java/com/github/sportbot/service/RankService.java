package com.github.sportbot.service;

import com.github.sportbot.config.WorkoutProperties;
import com.github.sportbot.exception.RankNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Rank;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserRank;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.RankRepository;
import com.github.sportbot.repository.UserRankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RankService {

    private final RankRepository rankRepository;
    private final UserRankRepository userRankRepository;
    private final MessageSource messageSource;
    private final UserService userService;
    private final EntityLocalizationService entityLocalizationService;
    private final WorkoutProperties workoutProperties;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ExerciseTypeService exerciseTypeService;

    /**
     * Calculates total XP for the user based on all exercise types.
     * XP = Σ(total_reps_per_exercise × coefficient)
     * Uses the same logic as LeaderboardService.getRating()
     */
    public double calculateTotalXP(User user) {
        return Stream.of("pull_up", "push_up", "squat", "abs")
                .mapToDouble(code -> {
                    ExerciseType type = exerciseTypeService.getExerciseType(code);
                    int totalReps = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, type);
                    return totalReps * workoutProperties.getCoefficient(code);
                })
                .sum();
    }

    /**
     * Assigns a global rank to the user based on their total XP across all exercise types.
     * The method is idempotent and will not duplicate assignments.
     * Note: If there are no global ranks configured, this method is a no-op
     * and returns an empty message.
     *
     * @return a localized promotion/next-rank message or empty string when nothing to report.
     */
    @Transactional
    public String assignRankIfEligible(User user) {
        double totalXP = calculateTotalXP(user);
        int xpThreshold = (int) Math.floor(totalXP);
        Locale locale = userService.getUserLocale(user);

        if (!rankRepository.existsByExerciseTypeIsNull()) {
            return "";
        }

        Rank currentRank = rankRepository
                .findTopByExerciseTypeIsNullAndThresholdLessThanEqualOrderByThresholdDesc(xpThreshold)
                .orElseThrow(() -> new RankNotFoundException("global", xpThreshold));

        Optional<UserRank> userHighestRank = userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user);

        if (isPromotion(user, userHighestRank, currentRank)) {
            saveUserRank(user, currentRank);
            return buildPromotionMessage(userHighestRank, currentRank, locale);
        } else {
            return buildNextRankHint(currentRank, xpThreshold, locale);
        }
    }

    private String buildNextRankHint(Rank achievedRank, int totalXP, Locale locale) {
        return rankRepository
                .findTopByExerciseTypeIsNullAndThresholdGreaterThanOrderByThresholdAsc(achievedRank.getThreshold())
                .map(next -> Math.max(0, next.getThreshold() - totalXP))
                .filter(remaining -> remaining > 0)
                .map(remaining -> messageSource.getMessage(
                        "workout.rank_next_left",
                        new Object[]{remaining},
                        locale))
                .orElse("");
    }

    private String buildPromotionMessage(Optional<UserRank> currentForType, Rank achievedRank, Locale locale) {
        Object previousTitle = currentForType
            .map(ur -> entityLocalizationService.getRankTitle(ur.getRank(), locale))
            .orElse("—");
        return messageSource.getMessage(
                "workout.rank_promoted",
                new Object[]{previousTitle, entityLocalizationService.getRankTitle(achievedRank, locale)},
                locale
        );
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
    public String getRankTitle(User user, Locale locale) {
        return userRankRepository.findTopByUserOrderByRank_ThresholdDesc(user)
                .map(ur -> entityLocalizationService.getRankTitle(ur.getRank(), locale))
                .orElse("-");
    }

}