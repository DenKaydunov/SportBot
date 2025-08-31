package com.github.sportbot.service;

import com.github.sportbot.WorkoutProperties;
import com.github.sportbot.dto.WorkoutPlanResponse;
import com.github.sportbot.exception.ProgramNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.UserMaxHistoryRepository;
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
    private final UserMaxHistoryRepository userMaxHistoryRepository;
    private final MessageSource messageSource;
    private final WorkoutProperties workoutProperties;
    private final ExerciseService exerciseService;
    private final UserService userService;

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

        UserProgramId id = new UserProgramId(user.getId(), exerciseType.getId());
        UserProgram program = userProgramRepository.findById(id)
                .orElseThrow(ProgramNotFoundException::new);

        int newDay = program.getDayNumber() + 1;
        program.setDayNumber(newDay);

        userProgramRepository.save(program);
    }


    private ProgramState loadProgramState(User user, ExerciseType exerciseType) {
        return userProgramRepository.findByIdUserIdAndIdExerciseTypeId(user.getId(), exerciseType.getId())
                .map(p -> new ProgramState(p.getCurrentMax(), p.getDayNumber()))
                .orElseGet(() -> {
                    int max = userMaxHistoryRepository.findByUserAndExerciseType(user, exerciseType).stream()
                            .mapToInt(UserMaxHistory::getMaxValue)
                            .max()
                            .orElse(5);

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
