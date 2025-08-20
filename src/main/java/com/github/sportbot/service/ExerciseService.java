package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private static final Logger log = LoggerFactory.getLogger(ExerciseService.class);

    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final UserProgramRepository userProgramRepository;

    @Transactional
    public void saveExerciseEntry(ExerciseEntryRequest req) {
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
    public void saveMaxEntry(ExerciseEntryRequest req) {
        log.info("Saving max entry: telegramId={}, exerciseType={}, max={}",
                req.telegramId(), req.exerciseType(), req.count());

        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        ExerciseType exerciseType = getExerciseType(req);

        UserMaxHistory max = UserMaxHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .maxValue(req.count())
                .date(LocalDate.now())
                .build();

        user.getMaxHistory().add(max);
        log.info("Added new UserMaxHistory for userId={}, exerciseTypeId={}, value={}",
                user.getId(), exerciseType.getId(), req.count());

        UserProgramId id = new UserProgramId(user.getId().longValue(), exerciseType.getId());
        UserProgram program = userProgramRepository.findById(id)
                .orElseGet(() -> {
                            UserProgram newProgram = new UserProgram();
                            newProgram.setId(id);
                            newProgram.setUser(user);
                            newProgram.setExerciseType(exerciseType);
                            newProgram.setDayNumber(1);
                            log.warn("No existing program found. Creating new program");
                            return newProgram;
                        }
                );

        program.setCurrentMax(req.count());
        userRepository.save(user);
        userProgramRepository.save(program);
    }
}
