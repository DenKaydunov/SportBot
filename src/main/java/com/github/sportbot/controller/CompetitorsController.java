package com.github.sportbot.controller;

import com.github.sportbot.service.CompetitorsService;
import io.swagger.v3.oas.annotations.Parameter;
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
            @Parameter(example = "123")
            Integer userId
    ) {
        return competitorsService.getCompetitorsAllTime(exerciseCode, userId);
    }


    @GetMapping("/v2/{exerciseCode}")
    public String getCompetitors(
            @PathVariable
            @Parameter(example = "squat")
            String exerciseCode,

            @RequestParam
            @Parameter(example = "1000001")
            Long telegramId
    ) {
        return competitorsService.getCompetitors(telegramId, exerciseCode);
    }


}
