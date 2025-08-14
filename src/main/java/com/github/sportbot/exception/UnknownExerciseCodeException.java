package com.github.sportbot.exception;

public class UnknownExerciseCodeException extends RuntimeException {

    private static final String UNKNOWN_EXERCISE_CODE_MESSAGE = "Unknown exercise code";

    public UnknownExerciseCodeException() {
        super(UNKNOWN_EXERCISE_CODE_MESSAGE);
    }
}
