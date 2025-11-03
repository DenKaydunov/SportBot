package com.github.sportbot.model;

import com.github.sportbot.exception.UnknownExerciseCodeException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum ExerciseTypeEnum {
    PUSH_UP("push_up"),
    PULL_UP("pull_up"),
    SQUAT("squat");

    private final String type;

    public static ExerciseTypeEnum getExerciseType(String code) {
        return Arrays.stream(values())
                .filter(e -> e.type.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new UnknownExerciseCodeException(code));
    }
}
