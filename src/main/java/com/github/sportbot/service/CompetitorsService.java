package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.repository.CompetitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CompetitorsService {

    private final CompetitorsRepository competitorsRepository;
    private final ExerciseTypeService exerciseTypeService;

    public String getCompetitorsAllTime(String exerciseCode, Integer userId) {

        ExerciseType exerciseType = exerciseTypeService.getExerciseType(exerciseCode);
        if (exerciseType == null) {
            throw new IllegalArgumentException(
                    "Exercise type not found for code=" + exerciseCode
            );
        }

        List<Object[]> rows =
                competitorsRepository.findCompetitors(userId, exerciseType.getId());

        List<Entry> entries = new ArrayList<>();
        for (Object[] row : rows) {
            entries.add(mapRow(row));
        }

        return formatCompetitorsString(exerciseType, userId, entries);
    }

    private Entry mapRow(Object[] row) {
        int position = ((Number) row[0]).intValue();
        int id = ((Number) row[1]).intValue();
        String name = (String) row[2];
        long total = ((Number) row[3]).longValue();

        return new Entry(position, id, name, total);
    }

    private String formatCompetitorsString(
            ExerciseType exerciseType,
            Integer userId,
            List<Entry> entries
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("🥊 Соперники 🥊\n");
        sb.append("Упражнение: ")
                .append(exerciseType.getTitle())
                .append("\n");
        sb.append("Период: всё время\n\n");

        if (entries.isEmpty()) {
            sb.append(
                    "Нет данных: пользователь не найден в таблице лидеров по этому упражнению."
            );
            return sb.toString();
        }

        for (Entry e : entries) {
            String marker = e.userId().equals(userId) ? "👉 " : "";
            sb.append(
                    String.format(
                            "%s%d. %s — %d%n",
                            marker,
                            e.position(),
                            safeName(e.name()),
                            e.total()
                    )
            );
        }

        return sb.toString();
    }

    private String safeName(String name) {
        return (name == null || name.isBlank()) ? "Без имени" : name;
    }

    /**
     * position — место в рейтинге
     * userId   — id пользователя
     * name     — имя пользователя
     * total    — общее количество повторений
     */
    private record Entry(
            Integer position,
            Integer userId,
            String name,
            Long total
    ) {}
}
