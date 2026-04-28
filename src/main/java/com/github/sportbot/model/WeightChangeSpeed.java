package com.github.sportbot.model;

import lombok.Getter;

@Getter
public enum WeightChangeSpeed {
    SLOW(0.25f),      // 0.25 kg/week
    OPTIMAL(0.5f),    // 0.5 kg/week (recommended)
    FAST(1.0f);       // 1 kg/week

    private final float kgPerWeek;

    WeightChangeSpeed(float kgPerWeek) {
        this.kgPerWeek = kgPerWeek;
    }
}
