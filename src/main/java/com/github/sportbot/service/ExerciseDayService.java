package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseDayRepository;
import com.github.sportbot.repository.ExerciseDaySummaryProjection;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseDayService {

    private final UserRepository userRepository;
    private final ExerciseDayRepository dayRepository;

    /**
     * return user exercises for the date

     * Твой прогресс за 25.02.2026💪🏻:
     * Приседания - 0
     * Подтягивания - 20
     * Отжимания - 10
     * Пресс - 0
     *
     */

    public List<ExerciseDaySummaryProjection>  getUserDayProgress(User user, LocalDate date){
        return dayRepository.getUserDayProgressByDate(user, date);
    }

    public String progressForDay(ExerciseEntryRequest req, LocalDate date){

        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        List<ExerciseDaySummaryProjection> summary = getUserDayProgress(user, date);

        DateTimeFormatter formated = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        StringBuilder report = new StringBuilder("Твой прогресс за " + date.format(formated) + "\uD83D\uDCAA\uD83C\uDFFB:\n");

        for (ExerciseDaySummaryProjection exercise : summary){
            report.append(exercise.getTitle())
                    .append(" - ")
                    .append(exercise.getTotalCount() + "\n");
        }
        return report.toString();
    }
}
