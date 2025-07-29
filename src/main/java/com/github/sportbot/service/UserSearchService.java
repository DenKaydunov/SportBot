package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private static final Logger log = LoggerFactory.getLogger(UserSearchService.class);

    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    public User findUserByTelegramId(Integer telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> {
                    log.warn("User not found for telegramId={}", telegramId);
                    return new IllegalArgumentException("User not found");
                });
    }

    public ExerciseType findExerciseTypeByCode(String exerciseCode) {
        return exerciseTypeRepository.findByCode(exerciseCode)
                .orElseThrow(() -> {
                    log.warn("Exercise type not found for code={}", exerciseCode);
                    return new IllegalArgumentException("Unknown exercise type");
                });
    }
}
