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
    private double incrementPerDay;
    private List<Double> coefficients;
    private Map<String, Double> coefficient;

    public double getCoefficient(String exerciseTypeCode) {
        return coefficient.getOrDefault(exerciseTypeCode, 1.0);
    }
}
