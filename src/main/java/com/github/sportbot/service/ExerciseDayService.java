package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseDayRepository;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseDayService {

    private final UserRepository userRepository;
    private final ExerciseDayRepository repository;
    private final ExerciseTypeRepository typeRepository;
    

    public String progressForDay(ExerciseEntryRequest req, LocalDate date){

        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        DateTimeFormatter formated = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        StringBuilder report = new StringBuilder("Твой прогресс за " + date.format(formated) + "\uD83D\uDCAA\uD83C\uDFFB:\n");

        List<ExerciseType> allTypes = typeRepository.findAll();

        List<Object[]> result = repository.sumByUserAndDate(user, date);

        Map<Long, Integer> sumMap = result.stream().
                collect(Collectors.toMap(
                        row -> ((ExerciseType) row[0]).getId(),
                        row -> (Integer) row[1]
                        ));

        for (ExerciseType type : allTypes){
            Integer total = sumMap.getOrDefault(type.getId(), 0);
            report.append(type.getTitle()).append(" - ").append(total + "\n");
        }

        return report.toString();

    }
}
