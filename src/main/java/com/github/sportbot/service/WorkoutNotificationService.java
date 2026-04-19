package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.dto.WorkoutEvent;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.integration.store.MessageGroup;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutNotificationService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final SportBot sportBot;
    private final MessageSource messageSource;
    private final EntityLocalizationService entityLocalizationService;

    public void processBatch(MessageGroup group) {

        List<WorkoutEvent> events = group.getMessages()
                .stream()
                .map(m -> (WorkoutEvent) m.getPayload())
                .toList();

        if (events.isEmpty()) return;

        WorkoutEvent first = events.getFirst();

        Integer ownerId = first.ownerUserId();
        Integer followerId = first.followerId();

        Map<ExerciseType, Integer> grouped = events.stream()
                .collect(Collectors.groupingBy(
                        WorkoutEvent::exerciseType,
                        Collectors.summingInt(WorkoutEvent::count)
                ));

        sendWorkoutSummary(ownerId, followerId, grouped);
    }

    private void sendWorkoutSummary(Integer ownerId,
                                    Integer followerId,
                                    Map<ExerciseType, Integer> exercises) {

        User owner = userRepository.findById(ownerId).orElseThrow();
        User follower = userRepository.findById(followerId).orElseThrow();

        Locale locale = userService.getUserLocale(follower);

        String message = buildMessage(owner, exercises, locale);

        sportBot.sendTgMessage(follower.getTelegramId(), message);
    }

    private String buildMessage(User user,
                                Map<ExerciseType, Integer> exercises,
                                Locale locale) {

        String header = messageSource.getMessage(
                "notification.friend.workout",
                new Object[]{user.getFullName()},
                locale
        );

        String body = exercises.entrySet().stream()
                .map(e -> messageSource.getMessage(
                        "notification.friend.workout.exercise",
                        new Object[]{
                                entityLocalizationService.getExerciseTypeTitle(e.getKey(), locale),
                                e.getValue()
                        },
                        locale
                ))
                .collect(Collectors.joining("\n"));

        return header + "\n" + body;
    }
}