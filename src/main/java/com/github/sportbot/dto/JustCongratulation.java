package com.github.sportbot.dto;

import java.util.List;
import java.util.Map;

public record JustCongratulation(
        String exerciseType,
        Map<Integer, List<String>> thresholdsToUsers
)
{}


