package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.config.SupportedLanguagesProvider;
import com.github.sportbot.dto.WorkoutEvent;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WorkoutNotificationService implements MessageLocalizer {

    private final UserRepository userRepository;
    private final SportBot sportBot;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;
    private final SupportedLanguagesProvider languagesProvider;

    public WorkoutNotificationService(UserRepository userRepository,
                                      @Lazy SportBot sportBot,
                                      MessageSource messageSource,
                                      EntityLocalizationService entityLocalizationService, LocaleResolver localeResolver, SupportedLanguagesProvider supportedLanguagesProvider) {
        this.userRepository = userRepository;
        this.sportBot = sportBot;
        this.messageSource = messageSource;
        this.entityLocalizationService = entityLocalizationService;
        this.languagesProvider = supportedLanguagesProvider;
    }

    public void processBatch(List<WorkoutEvent> events) {
        if (events == null || events.isEmpty()) return;

        WorkoutEvent context = events.getFirst();

        Map<ExerciseType, Integer> grouped = groupEvents(events);

        sendWorkoutSummary(
                context.ownerUserId(),
                context.followerId(),
                grouped
        );
    }

    private Map<ExerciseType, Integer> groupEvents(List<WorkoutEvent> events) {
        return events.stream()
                .collect(Collectors.groupingBy(
                        WorkoutEvent::exerciseType,
                        Collectors.summingInt(WorkoutEvent::count)
                ));
    }

    private void sendWorkoutSummary(Integer ownerId,
                                    Integer followerId,
                                    Map<ExerciseType, Integer> exercises) {

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found: " + ownerId));
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("follower not found: " + followerId));

        Locale locale = languagesProvider.getLocale(follower.getLanguage());

        String message = buildMessage(owner, exercises, locale);

        sportBot.sendTgMessage(follower.getTelegramId(), message);
    }

    private String buildMessage(User user,
                                Map<ExerciseType, Integer> exercises,
                                Locale locale) {

        String header = localize(
                "notification.subscription.workout.completed.title",
                new Object[]{user.getFullName()},
                locale
        );

        String body = exercises.entrySet().stream()
                .map(e -> localize(
                        "notification.subscription.workout.completed.exercise",
                        new Object[]{
                                entityLocalizationService.getExerciseTypeTitle(e.getKey(), locale),
                                e.getValue()
                        },
                        locale
                ))
                .collect(Collectors.joining("\n"));

        return header + "\n" + body;
    }

    @Override
    public String localize(String messageKey, Object[] context, Locale locale) {
        return messageSource.getMessage(messageKey, context, locale);
    }
}