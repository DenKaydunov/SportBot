package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorProjection;
import com.github.sportbot.repository.CompetitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CompetitorsService {

    private final CompetitorsRepository competitorsRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final UserService userService;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;

    public String getCompetitorsAllTime(String exerciseCode, Long telegramId) {
        User currentUser = userService.getUserByTelegramId(telegramId);
        Locale locale = userService.getUserLocale(currentUser);
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        List<CompetitorProjection> competitors =
                competitorsRepository.findCompetitors(currentUser.getId(), exerciseType.getId());
        return formatCompetitorsResponse(exerciseType, currentUser.getId(), competitors, locale);
    }

    private String formatCompetitorsResponse(
            ExerciseType type,
            Integer currentUserId,
            List<CompetitorProjection> competitors,
            Locale locale
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(messageSource.getMessage("competitors.header", null, locale)).append("\n");
        sb.append(messageSource.getMessage("competitors.exercise.label",
            new Object[]{entityLocalizationService.getExerciseTypeTitle(type, locale)}, locale)).append("\n");
        sb.append(messageSource.getMessage("competitors.period.all.time", null, locale))
            .append("\n\n");

        if (competitors.isEmpty()) {
            sb.append(messageSource.getMessage("competitors.no.data", null, locale));
            return sb.toString();
        }

        for (CompetitorProjection row : competitors) {
            boolean isMe = row.getUserId().equals(currentUserId);
            String marker = isMe ? "👉 " : "";

            sb.append(String.format(
                    "%s%d. %s — %d%n",
                    marker,
                    row.getPosition(),
                    row.getFullName(),
                    row.getTotal()
            ));
        }

        return sb.toString();
    }
}