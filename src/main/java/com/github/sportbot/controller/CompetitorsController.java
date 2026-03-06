package com.github.sportbot.controller;

import com.github.sportbot.service.CompetitorsService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/competitors")
@RequiredArgsConstructor
public class CompetitorsController {

    private final CompetitorsService competitorsService;

    @GetMapping("/{exerciseCode}")
    public String getCompetitorsAllTime(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam
            @Parameter(example = "1000001") @NotNull
            Long telegramId
    ) {
        return competitorsService.getCompetitorsAllTime(exerciseCode, telegramId);
    }
}
