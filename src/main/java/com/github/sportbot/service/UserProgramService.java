package com.github.sportbot.service;

import com.github.sportbot.WorkoutProperties;
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
        ProgramState programState = loadProgramState(user, exerciseType);

        List<Integer> sets = generateWorkoutSets(programState.max(), programState.day());
        int total = sets.stream().mapToInt(Integer::intValue).sum();

        String msg = localizeWorkoutMessage(sets, total);

        return new WorkoutPlanResponse(sets, total, msg);
    }

    /**
     * Обновление программы (инкремент дня)
     */
    public void updateProgram(Integer telegramId, String exerciseCode) {
        User user = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseService.getExerciseType(exerciseCode);

        UserProgram program = userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId())
                .orElse(createDefaultProgram(user, exerciseType));

        incrementProgramDayNumber(program);
        userProgramRepository.save(program);
    }

    private static void incrementProgramDayNumber(UserProgram program) {
        int newDay = program.getDayNumber() + 1;
        program.setDayNumber(newDay);
    }

    private UserProgram createDefaultProgram(User user, ExerciseType exerciseType) {
        int dayNumber = 1;
        UserProgramId id = new UserProgramId(user.getId(), exerciseType.getId());
        return UserProgram.builder()
                .id(id)
                .user(user)
                .exerciseType(exerciseType)
                .currentMax(userMaxService.getMax(user, exerciseType))
                .dayNumber(dayNumber)
                .build();
    }


    private ProgramState loadProgramState(User user, ExerciseType exerciseType) {
        return userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId())
                .map(p -> new ProgramState(p.getCurrentMax(), p.getDayNumber()))
                .orElseGet(() -> {
                    int max = userMaxService.getMax(user, exerciseType);
                    return new ProgramState(max, 1);
                });
    }

    private List<Integer> generateWorkoutSets(int max, int day) {
        double increment = workoutProperties.getIncrementPerDay() * day;

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

    /**
     * Record для хранения состояния программы.
     */
    private record ProgramState(int max, int day) {
    }

}
