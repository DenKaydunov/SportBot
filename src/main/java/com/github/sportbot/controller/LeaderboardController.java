package com.github.sportbot.controller;

import com.github.sportbot.service.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping("/{exerciseCode}")
    public ResponseEntity<String> getLeaderboard(
            @PathVariable String exerciseCode,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String period) {
        String result = leaderboardService.getLeaderboardString(exerciseCode, limit, period);
        return ResponseEntity.ok(result);
    }
}
