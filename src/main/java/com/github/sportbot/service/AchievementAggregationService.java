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

        // Получаем итоги до начала месяца и после окончания месяца
        List<UserExerciseSummary> totalsBeforeMonth = exerciseRecordRepository.getTotalBeforeDate(startDay);
        List<UserExerciseSummary> totalsAfterMonth = exerciseRecordRepository.getTotalBeforeDate(endDay.plusDays(1));

        List<Congratulation> achievementsList = buildAchievementsListWithMilestones(
                totalsBeforeMonth, totalsAfterMonth, targets);
        return messageBuild(achievementsList);
    }

    /**
     * Строит список поздравлений для пользователей, которые достигли новых отметок в течение месяца.
     * Сравнивает итоги до начала месяца с итогами после окончания месяца.
     */
    private List<Congratulation> buildAchievementsListWithMilestones(
            List<UserExerciseSummary> totalsBeforeMonth,
            List<UserExerciseSummary> totalsAfterMonth,
            List<Integer> targets) {

        // Создаем Map для быстрого поиска "до" значений по ключу user+exerciseType
        Map<String, Long> beforeMap = totalsBeforeMonth.stream()
                .collect(Collectors.toMap(
                        summary -> getKey(summary.user(), summary.exerciseType()),
                        UserExerciseSummary::total,
                        (a, b) -> a
                ));

        // Фильтруем только подписанных пользователей и находим тех, кто пересек новую отметку
        Map<String, Map<Integer, List<String>>> achievementsMap = totalsAfterMonth.stream()
                .filter(summary -> userService.isSubscribedUser(summary.user().getTelegramId()))
                .map(afterSummary -> {
                    String key = getKey(afterSummary.user(), afterSummary.exerciseType());
                    long totalBefore = beforeMap.getOrDefault(key, 0L);
                    long totalAfter = afterSummary.total();

                    // Определяем, какую новую отметку пользователь достиг
                    int achievedTarget = findNewlyAchievedTarget(totalBefore, totalAfter, targets);

                    if (achievedTarget > 0) {
                        return Optional.of(new AchievementRow(
                                afterSummary.exerciseType().getTitle(),
                                achievedTarget,
                                afterSummary.user().getFullName()
                        ));
                    }
                    return Optional.<AchievementRow>empty();
                })
                .flatMap(Optional::stream)
                .collect(Collectors.groupingBy(
                        AchievementRow::type,
                        Collectors.groupingBy(
                                AchievementRow::target,
                                Collectors.mapping(AchievementRow::fullName, Collectors.toList())
                        )
                ));

        return achievementsMap.entrySet().stream()
                .map(entry -> new Congratulation(entry.getKey(), entry.getValue()))
                .toList();
    }

    /**
     * Создает уникальный ключ для комбинации пользователь + тип упражнения
     */
    private String getKey(User user, ExerciseType exerciseType) {
        return user.getTelegramId() + "_" + exerciseType.getId();
    }

    /**
     * Находит максимальную отметку, которую пользователь достиг в течение периода.
     * Возвращает 0, если пользователь не пересек ни одной новой отметки.
     * Пример: если было 600, стало 1500, а отметки [500, 1000, 2000],
     * то вернет 1000 (т.к. 500 уже была достигнута, 1000 - новая, 2000 еще не достигнута)
     */
    private int findNewlyAchievedTarget(long totalBefore, long totalAfter, List<Integer> targets) {
        return targets.stream()
                .filter(target -> totalBefore < target && totalAfter >= target)
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
