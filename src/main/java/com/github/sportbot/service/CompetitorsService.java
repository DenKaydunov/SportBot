package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.CompetitorProjection;
import com.github.sportbot.repository.CompetitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitorsService {

    private final CompetitorsRepository competitorsRepository;
    private final ExerciseTypeService exerciseTypeService;
    private final UserService userService;

    public String getCompetitorsAllTime(String exerciseCode, Long telegramId) {
        User currentUser = userService.getUserByTelegramId(telegramId);
        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        List<CompetitorProjection> competitors =
                competitorsRepository.findCompetitors(currentUser.getId(), exerciseType.getId());
        return formatCompetitorsResponse(exerciseType, currentUser.getId(), competitors);
    }

    private String formatCompetitorsResponse(
            ExerciseType type,
            Integer currentUserId,
            List<CompetitorProjection> competitors
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("   ⚡ Соперники ⚡\n");
        sb.append("Упражнение: ").append(type.getTitle()).append("\n");
        sb.append("Период: Всё время\n\n");

        if (competitors.isEmpty()) {
            sb.append("Данных пока нет. Стань первым в этом списке! 💪");
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