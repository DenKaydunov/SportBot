package com.github.sportbot.service;

import com.github.sportbot.dto.AchievementRow;
import com.github.sportbot.dto.Congratulation;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.Targets;
import com.github.sportbot.model.UserExerciseTotal;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.TargetsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementAggregationService {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final TargetsRepository targetsRepository;

    public String getAchievementForMonth(){
        LocalDate startDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endDay = LocalDate.now().withDayOfMonth(1).minusDays(1);

        List<Integer> thresholds = targetsRepository.findAll()
                                                       .stream()
                                                       .map(Targets::getValue)
                                                       .sorted()
                                                       .toList();

        List<UserExerciseTotal> totals = exerciseRecordRepository.getTotalForMonth(startDay, endDay);

        List<Congratulation> achievementsList = buildAchievementsList(totals, thresholds);

        return messageBuild(achievementsList);
    }

    private List<Congratulation> buildAchievementsList(List<UserExerciseTotal> totals,
                                                       List<Integer> targets)
    {
        Map<String, Map<Integer, List<String>>> map = totals.stream()
                .filter(this::isSubscribedUser)
                .map(record -> toAchievementRow(record, targets))
                .flatMap(Optional::stream)
                .filter(row -> row.target() > 0)
                .collect(Collectors.groupingBy(
                        AchievementRow::type,
                        Collectors.groupingBy
                                (AchievementRow::target,
                                Collectors.mapping(AchievementRow::fullName, Collectors.toList())
                        )
                ));

        return map.entrySet().stream()
                .map(k ->new Congratulation(k.getKey(), k.getValue()))
                .collect(Collectors.toList());
    }

    private Optional<AchievementRow> toAchievementRow(UserExerciseTotal record, List<Integer> targets) {
        return resolveExerciseType(record)
                .map(type -> new AchievementRow(
                        type,
                        resolveTarget(record.total(), targets),
                        record.user().getFullName()
                ));
    }

    private Optional<String> resolveExerciseType(UserExerciseTotal record){
        return Optional.ofNullable(record.exerciseType())
                .map(ExerciseType::getTitle)
                .filter(title -> !title.isBlank());
    }

    private int resolveTarget(Long total, List<Integer> targets){
        return targets.stream()
                .filter(t -> t.longValue() <= total)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private boolean isSubscribedUser(UserExerciseTotal record){
        return record.user().getIsSubscribed();
    }

    /**
     *
     * @param monthlyAchievements
     * @return Exemple:
     * <p>
     * 🏆 Поздравляем чемпионов SportBot! 🏆
     * <p>
     * 🔥 Подтягивания
     * 2 000+: Max, Andrii
     * 1 000+: Ivan
     * <p>
     * 🔥 Отжимания
     * 10 000+: Iван
     * 5 000+: Aliaksandr M, Ivan
     * 3 000+: Игорь Куприяненко, Настя
     * 1 000+: Andrii, Alexander
     * 500+: Andrew, Burlia Oleg
     * <p>
     * 🔥 Приседания
     * 6 000+: Denis Kaydunov
     * 2 000+: Сергій, Настя
     * 1 000+: Andrii
     * 500+: Михаил Буткевич
     * <p>
     * 🔥 Пресс
     * 1 500+: Настя, Denis Kaydunov
     * 1 000+: Сергій
     * 500+: Михаил Буткевич
     */
    private String messageBuild(List<Congratulation> monthlyAchievements){
        StringBuilder sb = new StringBuilder("🏆 Поздравляем чемпионов SportBot! 🏆\n\n");

        for (Congratulation block : monthlyAchievements){
            sb.append(String.format("🔥%s%n", block.exerciseType()));
            for (Map.Entry<Integer, List<String>> map : block.targetsToUsers().entrySet()){
                Integer targets = map.getKey();
                List<String> users = map.getValue();
                sb.append(targets + "+: ");
                sb.append(String.join(", ", users));
                sb.append("\n");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
