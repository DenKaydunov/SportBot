package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.dto.AchievementRow;
import com.github.sportbot.dto.AchievementSendResponse;
import com.github.sportbot.dto.Congratulation;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.AchievementTarget;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserExerciseSummary;
import com.github.sportbot.repository.ExerciseRecordRepository;
import com.github.sportbot.repository.TargetsRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementAggregationService {
    private final ExerciseRecordRepository exerciseRecordRepository;
    private final TargetsRepository targetsRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SportBot sportBot;

    public String getAchievementForMonth(){
        LocalDate startDay = LocalDate.now().minusMonths(1).withDayOfMonth(1);
        LocalDate endDay = LocalDate.now().withDayOfMonth(1).minusDays(1);

        List<Integer> targets = targetsRepository.findAll()
                                                 .stream()
                                                 .map(AchievementTarget::getValue)
                                                 .sorted()
                                                 .toList();

        List<UserExerciseSummary> totals = exerciseRecordRepository.getTotalForMonth(startDay, endDay);
        List<Congratulation> achievementsList = buildAchievementsList(totals, targets);
        return messageBuild(achievementsList);
    }

    private List<Congratulation> buildAchievementsList(List<UserExerciseSummary> totals,
                                                       List<Integer> targets)
    {
        Map<String, Map<Integer, List<String>>> map = totals.stream()
                .filter(projection -> userService.isSubscribedUser(projection.user().getTelegramId()))
                .map(projection -> toAchievementRow(projection, targets))
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
                .toList();
    }

    private Optional<AchievementRow> toAchievementRow(UserExerciseSummary projection, List<Integer> targets) {
        return resolveExerciseType(projection)
                .map(type -> new AchievementRow(
                        type,
                        resolveTarget(projection.total(), targets),
                        projection.user().getFullName()
                ));
    }

    private Optional<String> resolveExerciseType(UserExerciseSummary total){
        return Optional.ofNullable(total.exerciseType())
                .map(ExerciseType::getTitle)
                .filter(title -> !title.isBlank());
    }

    private int resolveTarget(Long total, List<Integer> targets){
        return targets.stream()
                .filter(t -> t.longValue() <= total)
                .max(Integer::compareTo)
                .orElse(0);
    }

    /**
     * Метод для получения списка пользователей и их достижений за месяц
     *
     * @param monthlyAchievements
     * @return Example:
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
                sb.append(targets);
                sb.append("+: ");
                sb.append(String.join(", ", users));
                sb.append("\n");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }

    /**
     * Отправляет поздравления с достижениями за прошлый месяц всем подписанным пользователям
     *
     * @return статистика отправки сообщений
     */
    public AchievementSendResponse sendAchievementCongratulation() {
        log.info("Starting achievement congratulations sending");

        String message = getAchievementForMonth();

        if (message.isEmpty()) {
            log.info("No achievements to send - message is empty");
            return AchievementSendResponse.noContent();
        }

        List<User> subscribedUsers = userRepository.findAllByIsSubscribedTrue();
        log.info("Sending achievement congratulations to {} subscribed users", subscribedUsers.size());

        int successCount = 0;
        int failedCount = 0;

        for (User user : subscribedUsers) {
            try {
                log.debug("Sending achievement message to user: {} (telegramId: {})",
                         user.getFullName(), user.getTelegramId());
                sportBot.sendTgMessage(user.getTelegramId(), message);
                successCount++;
                log.debug("Successfully sent to user: {} (telegramId: {})",
                         user.getFullName(), user.getTelegramId());
            } catch (Exception e) {
                log.error("Failed to send achievement congratulations to user {}: {}",
                         user.getTelegramId(), e.getMessage());
                failedCount++;
            }
        }

        log.info("Achievement sending completed: {} successful, {} failed", successCount, failedCount);
        return AchievementSendResponse.success(successCount, failedCount);
    }
}
