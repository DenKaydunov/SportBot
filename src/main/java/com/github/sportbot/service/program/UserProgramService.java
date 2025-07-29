package com.github.sportbot.service.program;

import com.github.sportbot.model.*;
import com.github.sportbot.repository.UserProgramRepository;
import com.github.sportbot.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProgramService {

    private static final Logger log = LoggerFactory.getLogger(UserProgramService.class);

    private final UserSearchService userSearchService;
    private final UserProgramRepository userProgramRepository;

    public void updateProgram(Integer telegramId, String exerciseCode) {
        log.info("Updating program for telegramId={}, exerciseCode={}", telegramId, exerciseCode);

        User user = userSearchService.findUserByTelegramId(telegramId);
        ExerciseType exerciseType = userSearchService.findExerciseTypeByCode(exerciseCode);

        UserProgramId id = new UserProgramId(user.getId(), exerciseType.getId());
        UserProgram program = userProgramRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program not found"));

        int previousDay = program.getDayNumber();
        int newDay = previousDay + 1;
        program.setDayNumber(newDay);
        log.debug("Previous day: {}, incremented to: {}", previousDay, newDay);
        userProgramRepository.save(program);
    }
}
