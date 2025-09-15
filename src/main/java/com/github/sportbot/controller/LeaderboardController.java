package com.github.sportbot.controller;

import com.github.sportbot.service.LeaderboardService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    /**
     * Provides leaderboard data by period.
     * @see com.github.sportbot.model.Period  Period: today|week|month|all
     * @param exerciseCode
     * @param limit
     * @param period
     * @return
     */
    @GetMapping("/{exerciseCode}/by-period")
    public String getLeaderboardByPeriod(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam(defaultValue = "20")
            int limit,

            @RequestParam(required = false)
            @Parameter(example = "today")
            String period
    ) {
        return leaderboardService.getLeaderboardByPeriod(exerciseCode, limit, period);
    }

    /**
     * Provides leaderboard data by custom period, between startDate and endDate
     * @param exerciseCode
     * @param limit
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/{exerciseCode}/by-dates")
    public String getLeaderboardByDates(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam(defaultValue = "20")
            int limit,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(example = "2025-09-05")
            LocalDate startDate,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(example = "2025-09-05")
            LocalDate endDate
    ) {
        return leaderboardService.getLeaderboardByDates(exerciseCode, limit, startDate, endDate);
    }
}
