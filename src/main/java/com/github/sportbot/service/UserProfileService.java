package com.github.sportbot.service;

import com.github.sportbot.model.User;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

import static com.github.sportbot.model.ExerciseTypeEnum.*;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final MessageSource messageSource;
    private final RankService rankService;

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

        int countPushUps = exerciseService.getTotalReps(user, PUSH_UP);
        int countPullUps = exerciseService.getTotalReps(user, PULL_UP);
        int countSquats = exerciseService.getTotalReps(user, SQUAT);
        int countAbs = exerciseService.getTotalReps(user, ABS);

        int maxPushUps = userMaxService.getLastMaxByExerciseCode(user, PUSH_UP);
        int maxPullUps = userMaxService.getLastMaxByExerciseCode(user, PULL_UP);
        int maxSquats = userMaxService.getLastMaxByExerciseCode(user, SQUAT);
        int maxAbs = userMaxService.getLastMaxByExerciseCode(user, ABS);

        String rank = rankService.getRankTitle(user);

        return messageSource.getMessage(
                "profile.template",
                new Object[]{
                        user.getFullName(),
                        user.getRemindTime() != null ? user.getRemindTime().toString() : "не указано",
                        countPushUps, maxPushUps,
                        countPullUps, maxPullUps,
                        countSquats, maxSquats,
                        countAbs, maxAbs,
                        rank
                },
                locale
        );
    }


}
