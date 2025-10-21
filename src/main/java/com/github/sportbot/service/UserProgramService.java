package com.github.sportbot.service;

import com.github.sportbot.config.WorkoutProperties;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.UserProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class UserProgramService {

    public static final int FIRST_PROGRAM_DAY = 1;

    private final UserProgramRepository userProgramRepository;
    private final MessageSource messageSource;
    private final WorkoutProperties workoutProperties;
    private final ExerciseService exerciseService;
    private final UserService userService;
    private final UserMaxService userMaxService;

    /**
     * Получение плана тренировок для пользователя
     */
    public WorkoutPlanResponse getWorkoutPlan(Integer telegramId, String exerciseCode) {
        User user = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);

        UserProgram program = loadUserProgram(user, exerciseType);

        List<Integer> sets = calculateWorkoutSets(program.getCurrentMax(), program.getDayNumber());
        int total = sets.stream().mapToInt(Integer::intValue).sum();

        String msg = localizeWorkoutMessage(sets, total);
        return new WorkoutPlanResponse(sets, total, msg);
    }

    /**
     * Обновление программы (инкремент дня)
     */
    public void incrementDayProgram(Integer telegramId, String exerciseCode) {
        User user = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);

        UserProgram program = loadUserProgram(user, exerciseType);
        program.setDayNumber(program.getDayNumber() + 1);
        userProgramRepository.save(program);
    }

    private UserProgram createDefaultProgram(User user, ExerciseType exerciseType, int currentMax) {
        UserProgramId id = new UserProgramId(user.getId(), exerciseType.getId());
        return UserProgram.builder()
                .id(id)
                .user(user)
                .exerciseType(exerciseType)
                .currentMax(currentMax)
                .dayNumber(FIRST_PROGRAM_DAY)
                .build();
    }

    private UserProgram loadUserProgram(User user, ExerciseType exerciseType) {
        int lastMax = userMaxService.getLastMax(user, exerciseType);

        UserProgram program = userProgramRepository
                .findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId())
                .orElseGet(() -> createDefaultProgram(user, exerciseType, lastMax));

        if (!program.getCurrentMax().equals(lastMax)) {
            program.setCurrentMax(lastMax);
            program.setDayNumber(FIRST_PROGRAM_DAY);
            userProgramRepository.save(program);
        }

        return program;
    }

    private List<Integer> calculateWorkoutSets(int max, int day) {
        double increment = workoutProperties.getIncrementPerDay() * (day - 1);

        return workoutProperties.getCoefficients().stream()
                .map(coefficient -> (int) Math.round(max * (coefficient + increment)))
                .toList();
    }

    private String localizeWorkoutMessage(List<Integer> sets, int total) {
        return messageSource.getMessage(
                "workout.today_sets",
                new Object[]{sets.toString().replaceAll("[\\[\\]]", ""), total},
                Locale.forLanguageTag("ru-RU")
        );
    }

}
