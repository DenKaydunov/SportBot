package com.github.sportbot.service;

import com.github.sportbot.dto.MotivationResponse;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Motivation;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.MotivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MotivationService {

    private final MotivationRepository motivationRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    public MotivationResponse getMotivation(String exerciseCode) {
        ExerciseType exerciseType = exerciseTypeRepository.findByCode(exerciseCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown exercise code: " + exerciseCode));

        List<Motivation> motivations = motivationRepository.findByExerciseType(exerciseType);
        if (motivations.isEmpty()) {
            throw new NoSuchElementException("No motivation for exercise: " + exerciseCode);
        }

        int idx = ThreadLocalRandom.current().nextInt(motivations.size());
        Motivation m = motivations.get(idx);

        return new MotivationResponse(m.getMessage());
    }
}

