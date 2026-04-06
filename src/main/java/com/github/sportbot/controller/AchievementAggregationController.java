package com.github.sportbot.controller;

import com.github.sportbot.dto.AchievementSendResponse;
import com.github.sportbot.service.AchievementAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/achievement")
@RequiredArgsConstructor
@Slf4j
public class AchievementAggregationController {

    private final AchievementAggregationService aggregationService;

    @Operation(summary = "Получить список полученных достижений за последний месяц")
    @GetMapping("/congratulation")
    public String getAchievementForMonth(){
        return aggregationService.getAchievementForMonth();
    }

    @Operation(summary = "Отправить поздравления с достижениями всем подписанным пользователям")
    @PostMapping("/congratulation")
    public ResponseEntity<AchievementSendResponse> sendAchievementCongratulations() {
        log.info("Manual achievement sending triggered via REST API");
        AchievementSendResponse response = aggregationService.sendAchievementCongratulation();
        log.info("Manual achievement sending completed: {}", response);
        return ResponseEntity.ok(response);
    }
}
