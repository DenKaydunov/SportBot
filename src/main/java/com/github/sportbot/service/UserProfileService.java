package com.github.sportbot.service;

import com.github.sportbot.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    public static final String PUSH_UP = "push_up";
    public static final String PULL_UP = "pull_up";
    public static final String SQUAT = "squat";
    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final MessageSource messageSource;

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
     * <p>
     * 📊 Статус: Сегодня тренируем силу воли 💪
     *
     */
    public String getProfile(Integer telegramId, String lang) {
        Locale locale = Locale.forLanguageTag(lang);
        User user = userService.getUserByTelegramId(telegramId);

        int countPushUps = exerciseService.getTotalReps(user, PUSH_UP);
        int countPullUps = exerciseService.getTotalReps(user, PULL_UP);
        int countSquats = exerciseService.getTotalReps(user, SQUAT);

        int maxPushUps = userMaxService.getLastMaxByExerciseCode(user, PUSH_UP);
        int maxPullUps = userMaxService.getLastMaxByExerciseCode(user, PULL_UP);
        int maxSquats = userMaxService.getLastMaxByExerciseCode(user, SQUAT);

        String name = messageSource.getMessage("profile.name", new Object[]{user.getFullName()}, locale);
        String age = messageSource.getMessage("profile.age", null, locale);
        String gender = messageSource.getMessage("profile.gender", null, locale);
        String rank = messageSource.getMessage("profile.rank", null, locale);
        String achievements = messageSource.getMessage("profile.achievements", null, locale);
        String notificationTime = messageSource.getMessage("profile.notificationTime", new Object[]{user.getRemindTime().toString()}, locale);
        String pushUps = messageSource.getMessage("profile.push-ups", new Object[]{countPushUps, maxPushUps}, locale);
        String pullUps = messageSource.getMessage("profile.pullups", new Object[]{countPullUps, maxPullUps}, locale);
        String squats = messageSource.getMessage("profile.squats", new Object[]{countSquats, maxSquats}, locale);
        String status = messageSource.getMessage("profile.status", null, locale);

        return String.join("\n",
                name,
                age,
                gender,
                rank,
                achievements,
                notificationTime,
                "",
                "Всего/max",
                "* " + pushUps,
                "* " + pullUps,
                "* " + squats,
                "",
                status
        );
    }
}
