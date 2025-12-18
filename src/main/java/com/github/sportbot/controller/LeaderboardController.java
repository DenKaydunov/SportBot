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
     * @param exerciseCode type of exercise
     * @param limit count of rows
     * @param period period code (today, yesterday, week, month, all)
     * @return formatted leaderboard
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
     * Provides paginated leaderboard data by period.
     * @see com.github.sportbot.model.Period  Period: today|week|month|all
     * @param exerciseCode type of exercise
     * @param page zero-based page index
     * @param size page size
     * @param period period code (today, yesterday, week, month, all)
     * @return formatted leaderboard page
     */
    @GetMapping("/{exerciseCode}/by-period/paged")
    public String getLeaderboardByPeriodPaged(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size,

            @RequestParam(required = false)
            @Parameter(example = "today")
            String period
    ) {
        return leaderboardService.getLeaderboardByPeriodPaged(exerciseCode, page, size, period);
    }

    /**
     * Provides leaderboard data by custom period, between startDate and endDate
     * @param exerciseCode type of exercise
     * @param limit count of rows
     * @param startDate start Date
     * @param endDate end Date
     * @return formatted leaderboard
     */
    @GetMapping("/{exerciseCode}/by-dates")
    public String getLeaderboardByDates(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam(required = false)
            @Parameter(example = "PULL_UP_CH1")
            String tagCode,

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
        return leaderboardService.getLeaderboardByDates(exerciseCode, tagCode, limit, startDate, endDate);
    }
}
