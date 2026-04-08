package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.dto.ExerciseSummary;
import com.github.sportbot.service.CalendarService;
import com.github.sportbot.service.ExerciseService;
import com.github.sportbot.service.UserMaxService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserMaxService userMaxService;
    private final CalendarService calendarService;


    @PostMapping
    public String saveExerciseResult(@RequestBody @Valid ExerciseEntryRequest req) {
        return exerciseService.saveExerciseResult(req);
    }

    @PostMapping("/max")
    public String saveMax(@RequestBody @Valid ExerciseEntryRequest req) {
        return userMaxService.saveExerciseMaxResult(req);
    }

    @GetMapping("/progress/{telegramId}")
    public String progressForPeriod(
            @PathVariable
            @Parameter(example = "1000001")
            Long telegramId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(example = "2025-02-05")
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(example = "2026-02-05")
            LocalDate endDate
    ) {
        return exerciseService.progressForPeriod(telegramId, startDate, endDate);
    }

    @GetMapping("/today")
    public String progressToday(
            @RequestParam
            @Parameter(example = "1000001")
            Long telegramId){
        LocalDate date = LocalDate.now();
        return exerciseService.progressForPeriod(telegramId, date, null);
    }

    @GetMapping("/calendar/{telegramId}")
    public Map<String, List<ExerciseSummary>> getMonthData(
            @PathVariable
            @Parameter(example = "1000001")
            Long telegramId,

            @RequestParam
            @Parameter(example = "2026")
            Integer year,

            @RequestParam
            @Parameter(example = "6")
            Integer month
    ) {
        return calendarService.getMonthData(telegramId, year, month);
    }
}
