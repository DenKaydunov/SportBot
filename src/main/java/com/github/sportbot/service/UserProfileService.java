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

import static com.github.sportbot.model.ExerciseTypeEnum.*;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final ExerciseService exerciseService;

    private final UserService userService;
    private final UserMaxService userMaxService;
    private final UserRepository userRepository;
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
    public String getProfile(@NotNull Long telegramId, String lang) {
        User user = userService.getUserByTelegramId(telegramId);

        String ageValue = user.getAge() != null ? user.getAge().toString() : "не указан";
        String sexValue = mapSexToText(user.getSex());
        String languageValue = resolveLanguage(user.getLanguage(), lang);
        Locale locale = resolveLocale(user.getLanguage(), lang);
        String remindTimeValue = user.getRemindTime() != null ? user.getRemindTime().toString() : "не указано";

        int countPushUps = exerciseService.getTotalReps(user, PUSH_UP);
        int countPullUps = exerciseService.getTotalReps(user, PULL_UP);
        int countSquats = exerciseService.getTotalReps(user, SQUAT);

        int maxPushUps = userMaxService.getLastMaxByExerciseCode(user, PUSH_UP);
        int maxPullUps = userMaxService.getLastMaxByExerciseCode(user, PULL_UP);
        int maxSquats = userMaxService.getLastMaxByExerciseCode(user, SQUAT);

        return messageSource.getMessage(
                "profile.template",
                new Object[]{
                        user.getFullName(),
                        ageValue,
                        sexValue,
                        languageValue,
                        remindTimeValue,
                        countPushUps, maxPushUps,
                        countPullUps, maxPullUps,
                        countSquats, maxSquats
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
        userRepository.save(user);
        return messageSource.getMessage(
                "profile.updated",
                new Object[]{user.getFullName()},
                resolveLocale(user.getLanguage(), request.language())
        );
    }

    private String mapSexToText(Sex sex) {
        if (sex == null) {
            return "не указан";
        }
        return switch (sex) {
            case MAN -> "мужчина";
            case WOMAN -> "женщина";
        };
    }

    private String resolveLanguage(String storedLanguage, String requestedLang) {
        String lang = firstNonBlank(storedLanguage, requestedLang);
        if (lang == null) {
            return "не указан";
        }
        String lower = lang.toLowerCase(Locale.ROOT);
        if (Objects.equals(lower, "ru")) {
            return "русский";
        }
        return lower;
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
