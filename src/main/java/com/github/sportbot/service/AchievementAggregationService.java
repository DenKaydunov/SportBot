package com.github.sportbot.service;

import com.github.sportbot.dto.CongratulationBlock;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserExerciseTotal;
import com.github.sportbot.repository.ExerciseRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementAggregationService {
    private final ExerciseRecordRepository exerciseRecordRepository;

    private static final List<Integer> THRESHOLDS = List.of(500, 1_000, 5_000, 10_000, 20_000, 50_000);

    public List<CongratulationBlock> getMonthlyAchievements(){
        LocalDate start = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate end = LocalDate.now().withDayOfMonth(1).minusDays(1);

        List<UserExerciseTotal> totals = exerciseRecordRepository.getTotalForMonth(start, end);

        Map<String, Map<Integer, List<String>>> map = new HashMap<>();

        for (UserExerciseTotal record : totals){
            User user = record.user();
            ExerciseType type = record.exerciseType();
            Long total = record.total();
            if (!user.getIsSubscribed()){
                continue;
            }
            int threshold = THRESHOLDS.stream()
                    .filter(th -> th <= total)
                    .max(Integer::compareTo)
                    .orElse(0);

            if (threshold > 0){
                map.computeIfAbsent(type.getTitle(), k -> new HashMap<>())
                        .computeIfAbsent(threshold, k -> new ArrayList<>())
                        .add(user.getFullName());
            }
        }
        return map.entrySet().stream()
                .map(k ->new CongratulationBlock(k.getKey(), k.getValue()))
                .collect(Collectors.toList());
    }

    public String messageBuild(List<CongratulationBlock> monthlyAchievements){
        StringBuilder sb = new StringBuilder("🏆 Поздравляем чемпионов SportBot! 🏆\n\n");

        for (CongratulationBlock block : monthlyAchievements){
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
