package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserMaxHistory;
import com.github.sportbot.repository.UserMaxHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMaxService {



    public static final int DEFAULT_EXERCISE_VALUE = 5;
    private final UserMaxHistoryRepository userMaxHistoryRepository;

    public int getMax(User user, ExerciseType exerciseType) {
        return userMaxHistoryRepository.findByUserAndExerciseType(user, exerciseType).stream()
                .mapToInt(UserMaxHistory::getMaxValue)
                .max()
                .orElse(DEFAULT_EXERCISE_VALUE);
    }
}
