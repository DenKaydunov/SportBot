package com.github.sportbot.service;

import com.github.sportbot.exception.UnknownExerciseCodeException;
import com.github.sportbot.model.Motivation;
import com.github.sportbot.repository.MotivationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MotivationService {

    private final MotivationRepository motivationRepository;

    /**
     * Get random motivation message for exercise type in specified locale.
     * Falls back to 'ru' if no messages found for the requested locale.
     *
     * @param exerciseCode exercise code (push_up, pull_up, squat, abs)
     * @param locale language locale (ru, en, uk)
     * @return motivation message text
     */
    public String getMotivation(String exerciseCode, String locale) {
        Motivation motivation = motivationRepository
                .findRandomByExerciseTypeCodeAndLocale(exerciseCode, locale)
                .orElseGet(() -> {
                    // Fallback to 'ru' if no messages in requested locale
                    return motivationRepository
                            .findRandomByExerciseTypeCodeAndLocale(exerciseCode, "ru")
                            .orElseThrow(() -> new UnknownExerciseCodeException(exerciseCode));
                });
        return motivation.getMessage();
    }

    /**
     * @deprecated Use {@link #getMotivation(String, String)} instead
     */
    @Deprecated(since = "0022", forRemoval = true)
    public String getMotivation(String exerciseCode) {
        return getMotivation(exerciseCode, "ru");
    }
}

