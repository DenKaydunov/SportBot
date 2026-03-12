package com.github.sportbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "workout")
@Data
public class WorkoutProperties {
    public static final double DEFAULT_COEFFICIENT = 1.0;
    private double incrementPerDay;
    private List<Double> coefficients;
    private Map<String, Double> coefficient;

    public double getCoefficient(String exerciseTypeCode) {
        return coefficient.getOrDefault(exerciseTypeCode, DEFAULT_COEFFICIENT);
    }
}
