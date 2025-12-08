package com.github.sportbot.exception;

public class RankNotFoundException extends RuntimeException {

    private static final String RANK_NOT_FOUND_MESSAGE_TEMPLATE =
            "No rank found for type of exercise and total reps. Type: %s, Total reps: %d";

    public RankNotFoundException(String exerciseType, int totalReps) {
        super(String.format(
                RANK_NOT_FOUND_MESSAGE_TEMPLATE,
                exerciseType,
                totalReps
        ));
    }
}
