package com.github.sportbot.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @deprecated it looks unuseful, but it used in the tests
 */
@Getter
@RequiredArgsConstructor
@Deprecated(forRemoval = true)
public enum ExerciseTypeEnum {
    PUSH_UP("push_up"),
    PULL_UP("pull_up"),
    SQUAT("squat");

    private final String type;
}
