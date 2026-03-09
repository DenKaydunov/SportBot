package com.github.sportbot.controller;

import com.github.sportbot.service.AchievementAggregationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/achievement")
@RequiredArgsConstructor
public class AchievementAggregationController {

    private final AchievementAggregationService aggregationService;

    @GetMapping("/congratulation")
    public String getAchievementForMonth(){
        return aggregationService.getAchievementForMonth();
    }
}
