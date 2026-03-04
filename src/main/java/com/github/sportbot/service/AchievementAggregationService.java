package com.github.sportbot.service;

import com.github.sportbot.dto.JustCongratulation;
import com.github.sportbot.model.Thresholds;
import com.github.sportbot.model.UserExerciseTotal;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.ThresholdsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementAggregationService {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final ThresholdsRepository thresholdsRepository;

    public String getMonthlyAchievements(){
        LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(1).minusDays(1);

        List<Integer> thresholds = thresholdsRepository.findAll()
                                                       .stream()
                                                       .map(Thresholds::getValue)
                                                       .sorted()
                                                       .toList();

        List<UserExerciseTotal> totals = exerciseRecordRepository.getTotalForMonth(start, end);

        List<JustCongratulation> achievementsList = buildAchievementsList(totals, thresholds);

        return messageBuild(achievementsList);
    }

    private List<JustCongratulation> buildAchievementsList(List<UserExerciseTotal> totals,
                                                           List<Integer> thresholds)
    {
        Map<String, Map<Integer, List<String>>> map = totals.stream()
                .filter(record -> record.user().getIsSubscribed())
                .map(record -> new Object() {
            final String type = record.exerciseType() != null && record.exerciseType().getTitle() !=null
                    ? record.exerciseType().getTitle() : "UNKNOWN";
            final int threshold = thresholds.stream()
                    .filter(th -> th <= record.total())
                    .max(Integer::compareTo)
                    .orElse(0);
            final String fullName = record.user().getFullName();
    })
                .filter(x -> x.threshold > 0)
                .collect(Collectors.groupingBy(
                        x -> x.type,
                        Collectors.groupingBy(
                                x -> x.threshold,
                                Collectors.mapping(x -> x.fullName, Collectors.toList())
                        )
                ));
        return map.entrySet().stream()
                .map(k ->new JustCongratulation(k.getKey(), k.getValue()))
                .collect(Collectors.toList());
    }

    private String messageBuild(List<JustCongratulation> monthlyAchievements){
        StringBuilder sb = new StringBuilder("🏆 Поздравляем чемпионов SportBot! 🏆\n\n");

        for (JustCongratulation block : monthlyAchievements){
            sb.append(String.format("🔥%s%n", block.exerciseType()));
            for (Map.Entry<Integer, List<String>> map : block.thresholdsToUsers().entrySet()){
                Integer thresholds = map.getKey();
                List<String> users = map.getValue();
                sb.append(thresholds + "+: ");
                sb.append(String.join(", ", users));
                sb.append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}
