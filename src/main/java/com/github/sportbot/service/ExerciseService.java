package com.github.sportbot.service;

import com.github.sportbot.dto.ExerciseEntryRequest;
import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.ExerciseRecord;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final UserRepository userRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final MessageSource messageSource;


    @Transactional
    public String saveExerciseResult(ExerciseEntryRequest req) {
        User user = userRepository.findByTelegramId(req.telegramId())
                .orElseThrow(UserNotFoundException::new);

        ExerciseType exerciseType = getExerciseType(req);

        ExerciseRecord exercise = ExerciseRecord.builder()
                .user(user)
                .exerciseType(exerciseType)
                .count(req.count())
                .date(LocalDate.now())
                .build();

        user.getExerciseRecords().add(exercise);
        userRepository.save(user);

        int total = exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
        return messageSource.getMessage("workout.reps_recorded",
                new Object[]{exerciseType.getTitle(), req.count(), total},
                Locale.forLanguageTag("ru-RU"));
    }

    public ExerciseType getExerciseType(ExerciseEntryRequest req) {
        return getExerciseType(req.exerciseType());
    }

    //TODO move to ExerciseService
    public ExerciseType getExerciseType(String code) {
        return exerciseTypeRepository.findByCode(code)
                .orElseThrow(UnknownExerciseCodeException::new);
    }

    public int getTotalReps(User user, String exerciseCode) {
        ExerciseType exerciseType = getExerciseType(exerciseCode);
        return exerciseRecordRepository.sumTotalRepsByUserAndExerciseType(user, exerciseType);
    }
}
