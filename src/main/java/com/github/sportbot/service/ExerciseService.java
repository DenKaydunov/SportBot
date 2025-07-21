package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.DaoException;
import com.github.sportbot.model.*;
import com.github.sportbot.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    @Transactional
    public void saveExerciseEntry(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(() -> new DaoException("User not found"));

        ExerciseType exerciseType = exerciseTypeRepository.findByCode(req.exerciseType())
                .orElseThrow(() -> new DaoException("Unknown exercise code"));

        WorkoutHistory exercise = WorkoutHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(req.count())
                .date(LocalDate.now())
                .build();

        user.getWorkoutHistory().add(exercise);
        userRepository.save(user);
    }

    @Transactional
    public void saveMaxEntry(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(() -> new DaoException("User not found"));

        ExerciseType exerciseType = exerciseTypeRepository.findByCode(req.exerciseType())
                .orElseThrow(() -> new DaoException("Unknown exercise code"));

        UserMaxHistory max =  UserMaxHistory.builder()
                .user(user)
                .exerciseType(exerciseType)
                .maxValue(req.count())
                .date(LocalDate.now())
                .build();


        user.getMaxHistory().add(max);
        userRepository.save(user);
    }
}
