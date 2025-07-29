package com.github.sportbot.service.program;

import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.UserMaxHistoryRepository;
import com.github.sportbot.repository.UserProgramRepository;
import com.github.sportbot.service.UserSearchService;
import com.github.sportbot.WorkoutProperties;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserProgramFetchService {

    private static final Logger log = LoggerFactory.getLogger(UserProgramFetchService.class);

    private final UserSearchService userSearchService;
    private final UserProgramRepository userProgramRepository;
    private final UserMaxHistoryRepository userMaxHistoryRepository;
    private final MessageSource messageSource;
    private final WorkoutProperties workoutProperties;

    public WorkoutPlanResponse getWorkoutPlan(Integer telegramId, String exerciseCode) {
        log.info("Getting workout plan for telegramId={}, exerciseCode={}", telegramId, exerciseCode);
        User user = userSearchService.findUserByTelegramId(telegramId);
        ExerciseType exerciseType = userSearchService.findExerciseTypeByCode(exerciseCode);

        Pair<Integer, Integer> maxAndDay = userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId())
                .map(p -> Pair.of(p.getCurrentMax(), p.getDayNumber()))
                .orElseGet(() -> {
                    log.warn("Program not found. Using max from history and default day=1 for userId={}, exerciseTypeId={}",
                            user.getId(), exerciseType.getId());

                    int max = userMaxHistoryRepository.findByUserAndExerciseType(user, exerciseType).stream()
                            .mapToInt(UserMaxHistory::getMaxValue)
                            .max()
                            .orElse(5);

                    return Pair.of(max, 1);
                });

        int max = maxAndDay.getLeft();
        int day = maxAndDay.getRight();

        log.debug("Program loaded: max={}, day={}", max, day);

        List<Integer> sets = generateWorkoutSets(max, day);
        int total = sets.stream().mapToInt(Integer::intValue).sum();

        //TODO: TSP-168 Localizing API messages via MessageSource
        String msg = messageSource.getMessage(
                "workout.today_sets",
                new Object[]{sets.toString().replaceAll("[\\[\\]]", ""), total},
                Locale.forLanguageTag("ru-RU")
        );

        log.info("Workout sets: {}, total: {}, max: {}", sets, total, max);
        return new WorkoutPlanResponse(sets, total, msg);
    }

    private List<Integer> generateWorkoutSets(int max, int day) {
        double increment = workoutProperties.getIncrementPerDay() * day;

        return workoutProperties.getCoefficients().stream()
                .map(coefficient -> {
                    double coef = coefficient + increment;
                    return (int) Math.round(max * coef);
                })
                .toList();
    }
}
