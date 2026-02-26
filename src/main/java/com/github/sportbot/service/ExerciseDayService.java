package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseDaySummary;
import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseDayRepository;
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

     **/
    public String progressForDay(ExerciseEntryRequest req, LocalDate date){

        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        DateTimeFormatter formated = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        StringBuilder report = new StringBuilder("Твой прогресс за " + date.format(formated) + "\uD83D\uDCAA\uD83C\uDFFB:\n");

        List<ExerciseDaySummary> summary = dayRepository
                .getUserDayProgressBy(user, date)
                .stream()
                .map(r -> new ExerciseDaySummary(
                        r.getTitle(),
                        r.getTotalCount() == null ? 0 : r.getTotalCount()
                ))
                .toList();

        for (ExerciseDaySummary type : summary){
            report.append(type.title())
                    .append(" - ")
                    .append(type.totalCount() + "\n");
        }
        return report.toString();
    }
}
