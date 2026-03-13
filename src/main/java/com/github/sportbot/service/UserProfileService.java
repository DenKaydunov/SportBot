package com.github.sportbot.service;

import com.github.sportbot.dto.UpdateProfileRequest;
import com.github.sportbot.model.Sex;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    public static final String UNKNOWN_VALUE = "не указан";
    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final RankService rankService;
    private final StreakService streakService;
    private final LocaleService localeService;

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
        User user = userService.getUserByTelegramId(telegramId);


        String ageValue = user.getAge() != null ? user.getAge().toString() : UNKNOWN_VALUE;
        String sexValue = mapSexToText(user.getSex());
        String remindTimeValue = user.getRemindTime() != null ? user.getRemindTime().toString() : UNKNOWN_VALUE;

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
                        ageValue,
                        sexValue,
                        user.getLanguage(),
                        remindTimeValue,
                        countPushUps, maxPushUps,
                        countPullUps, maxPullUps,
                        countSquats, maxSquats,
                        countAbs, maxAbs,
                        rank,
                        streakInfo
                },
                localeService.getUserLocale(user)
        );
    }



    @Transactional
    public String updateProfile(UpdateProfileRequest request) {
        User user = userService.getUserByTelegramId(request.telegramId());
        if (request.age() != null) {
            user.setAge(request.age());
        }
        if (request.sex() != null) {
            user.setSex(request.sex());
        }
        if (request.language() != null) {
            String normalized = request.language().strip().toLowerCase(Locale.ROOT);
            user.setLanguage(normalized.isEmpty() ? null : normalized);
        }
        if (request.name() != null){
            user.setFullName(request.name());
        }
        userRepository.save(user);
        return messageSource.getMessage(
                "profile.updated",
                new Object[]{user.getFullName()},
                localeService.getUserLocale(user)
        );
    }

    private String mapSexToText(Sex sex) {
        if (sex == null) {
            return UNKNOWN_VALUE;
        }
        return switch (sex) {
            case MAN -> "мужчина";
            case WOMAN -> "женщина";
        };
    }

    private Locale resolveLocale(String storedLanguage, String requestedLang) {
        String lang = firstNonBlank(storedLanguage, requestedLang);
        if (lang == null || !lang.equalsIgnoreCase("ru")) {
            return Locale.forLanguageTag("ru");
        }
        return Locale.forLanguageTag("ru");
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.strip().isEmpty()) {
                return value;
            }
        }
        return null;
    }
}
