package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserMaxHistory;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.UserMaxHistoryRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserMaxService {

    public static final int DEFAULT_EXERCISE_VALUE = 5;

    private final UserService userService;
    private final ExerciseService exerciseService;
    private final UserRepository userRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final UserMaxHistoryRepository userMaxHistoryRepository;
    private final MessageSource messageSource;



    @Transactional
    public String saveExerciseMaxResult(ExerciseEntryRequest req) {
        int telegramId = req.telegramId();
        //TODO https://warsportbot.atlassian.net/browse/TSP-255
        int maxValue = req.count();

        User user = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseService.getExerciseType(req);

        UserMaxHistory userMaxHistory = UserMaxHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .maxValue(maxValue)
                .date(LocalDateTime.now())
                .build();

        user.getMaxHistory().add(userMaxHistory);

        exerciseService.saveExerciseResult(new ExerciseEntryRequest(telegramId, exerciseType.getCode(), maxValue));
        userRepository.save(user);

        int totalReps = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
        return messageSource.getMessage(
                "workout.max_reps",
                new Object[]{exerciseType.getTitle(), user.getFullName(), maxValue, totalReps},
                Locale.forLanguageTag("ru-RU")
        );
    }

    public int getLastMax(User user, ExerciseType exerciseType) {
        return userMaxHistoryRepository.findTopByUserAndExerciseTypeOrderByDateDesc(user, exerciseType)
                .map(UserMaxHistory::getMaxValue)
                .orElse(DEFAULT_EXERCISE_VALUE);
    }

    public int getLastMaxByExerciseCode(User user, ExerciseTypeEnum exerciseCode) {
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode.getType());
        return getLastMax(user, exerciseType);
    }
}
