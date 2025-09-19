package com.github.sportbot.service;

import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.Motivation;
import com.github.sportbot.repository.MotivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MotivationService {

    private final MotivationRepository motivationRepository;

    public String getMotivation(String exerciseCode) {
        Motivation motivation = motivationRepository.findRandomByExerciseTypeCode(exerciseCode)
                .orElseThrow(UnknownExerciseCodeException::new);
        return motivation.getMessage();
    }
}

