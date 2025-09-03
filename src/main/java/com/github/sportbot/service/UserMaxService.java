package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserMaxHistory;
import com.github.sportbot.repository.UserRepository;
import com.github.sportbot.repository.WorkoutHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserMaxService {

    private final UserProgramService userProgramService;
    private final UserService userService;
    private final ExerciseService exerciseService;
    private final UserRepository userRepository;
    private final WorkoutHistoryRepository workoutHistoryRepository;
    private final MessageSource messageSource;



    @Transactional
    public String saveExerciseMaxResult(ExerciseEntryRequest req) {
        int telegramId = req.telegramId();
        int maxValue = req.count();

        User user = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseService.getExerciseType(req);

        UserMaxHistory userMaxHistory = UserMaxHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .maxValue(maxValue)
                .date(LocalDate.now())
                .build();

        user.getMaxHistory().add(userMaxHistory);

        exerciseService.saveExerciseResult(new ExerciseEntryRequest(telegramId, exerciseType.getCode(), maxValue));
        userProgramService.updateProgram(user, exerciseType, maxValue);
        userRepository.save(user);

        int totalReps = workoutHistoryRepository.sumTotalReps(user, exerciseType);
        return messageSource.getMessage(
                "workout.max_reps",
                new Object[]{exerciseType.getTitle(), user.getFullName(), maxValue, totalReps},
                Locale.forLanguageTag("ru-RU")
        );
    }
}
