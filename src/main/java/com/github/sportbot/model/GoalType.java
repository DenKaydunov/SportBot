package com.github.sportbot.model;

public enum GoalType {
    LOSS,      // target < current
    MAINTAIN,  // target == current (±2kg tolerance)
    GAIN       // target > current
}
