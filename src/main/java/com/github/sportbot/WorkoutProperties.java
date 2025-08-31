package com.github.sportbot;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "workout")
@Data
public class WorkoutProperties {
    private double incrementPerDay;
    private List<Double> coefficients;
}
