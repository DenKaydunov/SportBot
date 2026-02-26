package com.github.sportbot.controller;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.service.ExerciseDayService;
import com.github.sportbot.service.ExerciseService;
import com.github.sportbot.service.UserMaxService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/v1/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final UserMaxService userMaxService;
    private final ExerciseDayService dayService;


    @PostMapping
    public String saveExerciseResult(@RequestBody @Valid ExerciseEntryRequest req) {
        return exerciseService.saveExerciseResult(req);
    }

    @PostMapping("/max")
    public String saveMax(@RequestBody @Valid ExerciseEntryRequest req) {
        return userMaxService.saveExerciseMaxResult(req);
    }

    @GetMapping("/day/{date}")
    public String progressForDay(@Valid ExerciseEntryRequest req,
                                 @PathVariable String date){
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return dayService.progressForDay(req, localDate);
    }
}
