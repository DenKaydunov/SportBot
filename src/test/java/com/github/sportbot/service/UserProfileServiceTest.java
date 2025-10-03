package com.github.sportbot.service;

import com.github.sportbot.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserService userService;
    @Mock
    private ExerciseService exerciseService;
    @Mock
    private UserMaxService userMaxService;
    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private UserProfileService userProfileService;

    @Test
    void getProfile_ReturnsFormattedProfile() {
        // Given
        Integer telegramId = 123456;
        String lang = "ru";
        User user = new User();
        user.setFullName("Denis Kaydunov");
        user.setRemindTime(java.time.LocalTime.of(13, 0));


        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, UserProfileService.PUSH_UP)).thenReturn(13663);
        when(exerciseService.getTotalReps(user, UserProfileService.PULL_UP)).thenReturn(2009);
        when(exerciseService.getTotalReps(user, UserProfileService.SQUAT)).thenReturn(2293);

        when(userMaxService.getLastMaxByExerciseCode(user, UserProfileService.PUSH_UP)).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, UserProfileService.PULL_UP)).thenReturn(15);
        when(userMaxService.getLastMaxByExerciseCode(user, UserProfileService.SQUAT)).thenReturn(50);

        Locale locale = Locale.forLanguageTag(lang);
        when(messageSource.getMessage(eq("profile.name"), any(), eq(locale))).thenReturn("📝 Имя: Denis Kaydunov");
        when(messageSource.getMessage(eq("profile.age"), any(), eq(locale))).thenReturn("📈 Возраст: не указан");
        when(messageSource.getMessage(eq("profile.gender"), any(), eq(locale))).thenReturn("🎭 Пол: не задан");
        when(messageSource.getMessage(eq("profile.rank"), any(), eq(locale))).thenReturn("⚔ Ранг: Жалкое млекопитающее 🐒");
        when(messageSource.getMessage(eq("profile.achievements"), any(), eq(locale))).thenReturn("🏆 Достижения: ещё впереди 🚀");
        when(messageSource.getMessage(eq("profile.notificationTime"), any(), eq(locale))).thenReturn("⏰ Время тренировки: 13:00");
        when(messageSource.getMessage(eq("profile.push-ups"), any(), eq(locale))).thenReturn("• Отжимания: 13663 (max: ещё не определён)");
        when(messageSource.getMessage(eq("profile.pullups"), any(), eq(locale))).thenReturn("• Подтягивания: 2009 (max: 15)");
        when(messageSource.getMessage(eq("profile.squats"), any(), eq(locale))).thenReturn("• Приседания: 2293 (max: 50)");
        when(messageSource.getMessage(eq("profile.status"), any(), eq(locale))).thenReturn("📊 Статус: Сегодня тренируем силу воли 💪");

        // When
        String profile = userProfileService.getProfile(telegramId, lang);

        // Then
        assertTrue(profile.contains("📝 Имя: Denis Kaydunov"));
        assertTrue(profile.contains("• Отжимания: 13663 (max: ещё не определён)"));
        assertTrue(profile.contains("• Подтягивания: 2009 (max: 15)"));
        assertTrue(profile.contains("• Приседания: 2293 (max: 50)"));
        assertTrue(profile.contains("📊 Статус: Сегодня тренируем силу воли 💪"));

        verify(userService).getUserByTelegramId(telegramId);
        verify(exerciseService).getTotalReps(user, UserProfileService.PUSH_UP);
        verify(exerciseService).getTotalReps(user, UserProfileService.PULL_UP);
        verify(exerciseService).getTotalReps(user, UserProfileService.SQUAT);
        verify(userMaxService).getLastMaxByExerciseCode(user, UserProfileService.PUSH_UP);
        verify(userMaxService).getLastMaxByExerciseCode(user, UserProfileService.PULL_UP);
        verify(userMaxService).getLastMaxByExerciseCode(user, UserProfileService.SQUAT);
    }

}