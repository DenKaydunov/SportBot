package com.github.sportbot.service;

import com.github.sportbot.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final MessageSource messageSource;
    private final RankService rankService;
    private final StreakService streakService;

    /**
     * Return user profile.
     *
     * @param telegramId - user id from telegram
     * @param lang - user language
     * @return Example:
     * <p>
     * 📝 Имя: Test User
     * 📈 Возраст: не указан
     * 🎭 Пол: не задан
     * 🌐 Язык: русский
     * ⚔ Ранг: Жалкое млекопитающее 🐒
     * 🏆 Достижения: ещё впереди 🚀
     * ⏰ Время тренировки: 13:00
     * <p>
     * 🏋️ Прогресс
     * • Отжимания: 13663 (max: ещё не определён)
     * • Подтягивания: 2009 (max: 15)
     * • Приседания: 2293 (max: 50)
     * • Пресс: 2293 (max: 50)
     * <p>
     * 📊 Статус: Сегодня тренируем силу воли 💪
     *
     */
    public String getProfile(@NotNull Long telegramId, String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        User user = userService.getUserByTelegramId(telegramId);

        int countPushUps = exerciseService.getTotalReps(user, "push_up");
        int countPullUps = exerciseService.getTotalReps(user, "pull_up");
        int countSquats = exerciseService.getTotalReps(user, "squat");
        int countAbs = exerciseService.getTotalReps(user, "abs");

        int maxPushUps = userMaxService.getLastMaxByExerciseCode(user, "push_up");
        int maxPullUps = userMaxService.getLastMaxByExerciseCode(user, "pull_up");
        int maxSquats = userMaxService.getLastMaxByExerciseCode(user, "squat");
        int maxAbs = userMaxService.getLastMaxByExerciseCode(user, "abs");

        String rank = rankService.getRankTitle(user);
        String streakInfo = streakService.getStreakInfo(user);

        return messageSource.getMessage(
                "profile.template",
                new Object[]{
                        user.getFullName(),
                        user.getRemindTime() != null ? user.getRemindTime().toString() : "не указано",
                        countPushUps, maxPushUps,
                        countPullUps, maxPullUps,
                        countSquats, maxSquats,
                        countAbs, maxAbs,
                        rank,
                        streakInfo
                },
                locale
        );
    }


}
