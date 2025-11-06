package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.repository.ExerciseTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;

    public ExerciseType getExerciseType(String code) {
        return exerciseTypeRepository.findByCode(code)
                .orElseThrow(() -> new UnknownExerciseCodeException(code));
    }

    public ExerciseType getExerciseType(ExerciseEntryRequest req) {
        ExerciseTypeEnum exerciseType = ExerciseTypeEnum.getExerciseType(req.exerciseType());
        String code = exerciseType.getType();
        return exerciseTypeRepository.findByCode(code)
                .orElseThrow(() -> new UnknownExerciseCodeException(code));
    }

}
