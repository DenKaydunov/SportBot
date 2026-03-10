package com.github.sportbot.model;

public record UserScore(
        User user,
        Double totalScore
) {
}
