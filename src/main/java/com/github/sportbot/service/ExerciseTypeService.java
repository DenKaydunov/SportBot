package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;

    public ExerciseType getExerciseType(String code) {
        return exerciseTypeRepository.findByCode(code)
                .orElseThrow(UnknownExerciseCodeException::new);
    }

    public ExerciseType getExerciseType(ExerciseEntryRequest req) {
        return getExerciseType(req.exerciseType());
    }

}
