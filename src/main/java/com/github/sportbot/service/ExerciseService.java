package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final UserProgramRepository userProgramRepository;
    private final WorkoutHistoryRepository workoutHistoryRepository;
    private final MessageSource messageSource;

    @Transactional
    public void saveExerciseResult(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        ExerciseType exerciseType = getExerciseType(req);

        WorkoutHistory exercise = WorkoutHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(req.count())
                .date(LocalDate.now())
                .build();

        user.getWorkoutHistory().add(exercise);
        userRepository.save(user);
    }

    public ExerciseType getExerciseType(ExerciseEntryRequest req) {
        return getExerciseType(req.exerciseType());
    }

    public ExerciseType getExerciseType(String code) {
        return exerciseTypeRepository.findByCode(code)
                .orElseThrow(UnknownExerciseCodeException::new);
    }

    @Transactional
    public String saveExerciseMaxResult(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        ExerciseType exerciseType = getExerciseType(req);

        UserMaxHistory userMaxHistory = UserMaxHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .maxValue(req.count())
                .date(LocalDate.now())
                .build();

        user.getMaxHistory().add(userMaxHistory);

        UserProgramId id = new UserProgramId(user.getId(), exerciseType.getId());
        UserProgram program = userProgramRepository.findById(id)
                .orElseGet(() -> {
                            UserProgram newProgram = new UserProgram();
                            newProgram.setId(id);
                            newProgram.setUser(user);
                            newProgram.setExerciseType(exerciseType);
                            newProgram.setDayNumber(1);
                            return newProgram;
                        }
                );

        program.setCurrentMax(req.count());
        userRepository.save(user);
        userProgramRepository.save(program);

        int totalReps = workoutHistoryRepository.sumAllByExerciseType(exerciseType.getId());
        return messageSource.getMessage(
                "workout.max_reps",
                new Object[]{exerciseType.getTitle(), user.getFullName(), userMaxHistory.getMaxValue(), totalReps},
                Locale.forLanguageTag("ru-RU")
        );
    }

}
