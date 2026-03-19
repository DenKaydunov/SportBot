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

@Service
@RequiredArgsConstructor
public class UserProfileService {

    public static final String PROFILE_UNKNOWN_VALUE = "profile.unknown.value";
    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final RankService rankService;
    private final StreakService streakService;

    /**
     * Return user profile.
     *
     * @param telegramId - user id from telegram
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
    public String getProfile(@NotNull Long telegramId) {
        User user = userService.getUserByTelegramId(telegramId);
        Locale locale = userService.getUserLocale(user);

        String ageValue = user.getAge() != null ? user.getAge().toString() : messageSource.getMessage(PROFILE_UNKNOWN_VALUE, null, locale);
        String sexValue = mapSexToText(user.getSex(), locale);
        String localeUser = resolveLanguage(user.getLanguage(), locale);
        String remindTimeValue = user.getRemindTime() != null ? user.getRemindTime().toString() : messageSource.getMessage(PROFILE_UNKNOWN_VALUE, null, locale);
        Integer balance = user.getBalanceTon();

        int countPushUps = exerciseService.getTotalReps(user, "push_up");
        int countPullUps = exerciseService.getTotalReps(user, "pull_up");
        int countSquats = exerciseService.getTotalReps(user, "squat");
        int countAbs = exerciseService.getTotalReps(user, "abs");

        int maxPushUps = userMaxService.getLastMaxByExerciseCode(user, "push_up");
        int maxPullUps = userMaxService.getLastMaxByExerciseCode(user, "pull_up");
        int maxSquats = userMaxService.getLastMaxByExerciseCode(user, "squat");
        int maxAbs = userMaxService.getLastMaxByExerciseCode(user, "abs");

        String rank = rankService.getRankTitle(user, locale);
        String streakInfo = streakService.getStreakInfo(user);

        double totalXP = rankService.calculateTotalXP(user);
        String xpFormatted = String.format("%.1f", totalXP);

        return messageSource.getMessage(
                "profile.template",
                new Object[]{
                        user.getFullName(),
                        ageValue,
                        sexValue,
                        localeUser,
                        remindTimeValue,
                        countPushUps, maxPushUps,
                        countPullUps, maxPullUps,
                        countSquats, maxSquats,
                        countAbs, maxAbs,
                        xpFormatted,
                        rank,
                        streakInfo,
                        balance
                },
                locale
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
        Locale locale = userService.getUserLocale(user);
        return messageSource.getMessage(
                "profile.updated",
                new Object[]{user.getFullName()},
                locale
        );
    }

    private String mapSexToText(Sex sex, Locale locale) {
        if (sex == null) {
            return messageSource.getMessage(PROFILE_UNKNOWN_VALUE, null, locale);
        }
        return switch (sex) {
            case MAN -> messageSource.getMessage("profile.gender.man", null, locale);
            case WOMAN -> messageSource.getMessage("profile.gender.woman", null, locale);
        };
    }

    private String resolveLanguage(String storedLanguage, Locale locale) {
        String lang = firstNonBlank(storedLanguage);
        if (lang == null) {
            return messageSource.getMessage(PROFILE_UNKNOWN_VALUE, null, locale);
        }
        String text = lang.toLowerCase(Locale.ROOT);
        String messageKey = switch (text) {
            case "ru" -> "profile.language.ru";
            case "en" -> "profile.language.en";
            case "uk" -> "profile.language.uk";
            default -> null;
        };

        return messageKey != null
            ? messageSource.getMessage(messageKey, null, locale)
            : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
