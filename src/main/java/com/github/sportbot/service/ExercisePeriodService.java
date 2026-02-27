package com.github.sportbot.service;

import com.github.sportbot.repository.ExercisePeriodRepository;
import com.github.sportbot.repository.ExercisePeriodProjection;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExercisePeriodService {

    private final UserRepository userRepository;
    private final ExercisePeriodRepository dayRepository;

    /**
     * return user exercises for the date

     * Твой прогресс за 25.02.2026:
     * Приседания - 0
     * Подтягивания - 20
     * Отжимания - 10
     * Пресс - 0
     *
     */
    public List<ExercisePeriodProjection> getUserPeriodProgress(Long telegramId,
                                                                LocalDate startDate,
                                                                LocalDate endDate)
    {
        return dayRepository.getUserProgressByPeriod(telegramId, startDate, endDate);
    }

    public String progressForPeriod(Long telegramId,
                                    String oneDate,
                                    String twoDate)
    {
        if(twoDate == null){
            twoDate = oneDate;
        }
        LocalDate startDate = LocalDate.parse(oneDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        LocalDate endDate = LocalDate.parse(twoDate, DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания!");
        }

        List<ExercisePeriodProjection> summary = getUserPeriodProgress(telegramId, startDate, endDate);

        DateTimeFormatter formated = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        StringBuilder report = new StringBuilder();

        if (startDate.equals(endDate)){
            report.append("Твой прогресс за "
                    + startDate.format(formated) + ":\n");
        }else {
            report.append("Твой прогресс c "
                    + startDate.format(formated) + " по " + endDate.format(formated) + ":\n");
        }


            for (ExercisePeriodProjection exercise : summary) {
                report.append(exercise.getExerciseType())
                        .append(" - ")
                        .append(exercise.getTotalCount() + "\n");
            }

        return report.toString();
    }
}
