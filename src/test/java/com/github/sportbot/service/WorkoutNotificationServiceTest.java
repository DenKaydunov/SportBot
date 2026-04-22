package com.github.sportbot.service;

import com.github.sportbot.bot.SportBot;
import com.github.sportbot.config.SupportedLanguagesProvider;
import com.github.sportbot.dto.WorkoutEvent;
import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.store.SimpleMessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkoutNotificationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SportBot sportBot;

    @Mock
    private MessageSource messageSource;

    @Mock
    private EntityLocalizationService entityLocalizationService;

    @Mock
    private SupportedLanguagesProvider languagesProvider;

    @Mock
    private LocaleResolver localeResolver;

    @InjectMocks
    private WorkoutNotificationService workoutNotificationService;

    @Test
    void processBatch_shouldAggregateAndSendSingleMessage() {

        // given
        User owner = User.builder()
                .id(1)
                .fullName("Иван")
                .telegramId(100L)
                .build();

        User follower = User.builder()
                .id(2)
                .telegramId(200L)
                .fullName("Follower")
                .language("ru")
                .build();

        ExerciseType exerciseType = ExerciseType.builder()
                .id(1L)
                .code("push_up")
                .title("Отжимания")
                .build();

        WorkoutEvent event1 = new WorkoutEvent(1, 2, exerciseType, 20);
        WorkoutEvent event2 = new WorkoutEvent(1, 2, exerciseType, 30);

        Message<WorkoutEvent> m1 = MessageBuilder.withPayload(event1).build();
        Message<WorkoutEvent> m2 = MessageBuilder.withPayload(event2).build();

        MessageGroup group = new SimpleMessageGroup(List.of(m1, m2), "test");

        when(userRepository.findById(1)).thenReturn(Optional.of(owner));
        when(userRepository.findById(2)).thenReturn(Optional.of(follower));

        when(languagesProvider.getLocale("ru")).thenReturn(Locale.forLanguageTag("ru"));

        when(entityLocalizationService.getExerciseTypeTitle(eq(exerciseType), any()))
                .thenReturn("Отжимания");

        when(messageSource.getMessage(eq("notification.subscription.workout.completed.title"), any(), any()))
                .thenReturn("Твой друг Иван выполнил тренировку:");

        when(messageSource.getMessage(eq("notification.subscription.workout.completed.exercise"), any(), any()))
                .thenAnswer(invocation -> {
                    Object[] args = invocation.getArgument(1);
                    return args[0] + " (" + args[1] + ")";
                });

        // when
        workoutNotificationService.processBatch(group);

        // then
        verify(sportBot, times(1))
                .sendTgMessage(eq(200L), contains("Отжимания (50)"));
    }
}
