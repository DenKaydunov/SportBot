package com.github.sportbot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExerciseTypeEnum {
    PUSH_UP("push_up"),
    PULL_UP("pull_up"),
    SQUAT("squat");

    private final String type;

    public static ExerciseTypeEnum fromCode(String code) {
        for (ExerciseTypeEnum exerciseCode : values()) {
            if (exerciseCode.type.equalsIgnoreCase(code)) {
                return exerciseCode;
            }
        }
        throw new IllegalArgumentException("Unknown exercise code: " + code);
    }
}
