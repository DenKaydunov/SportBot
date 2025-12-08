package com.github.sportbot.service;

import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    private UserService userService;
    private ExerciseService exerciseService;
    private UserMaxService userMaxService;
    private MessageSource messageSource;
    private RankService rankService;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource realMessageSource = new ResourceBundleMessageSource();
        realMessageSource.setBasename("messages");
        realMessageSource.setDefaultEncoding("UTF-8");
        this.messageSource = realMessageSource;

        this.userService = mock(UserService.class);
        this.exerciseService = mock(ExerciseService.class);
        this.userMaxService = mock(UserMaxService.class);
        this.rankService = mock(RankService.class);

        this.userProfileService = new UserProfileService(
                exerciseService,
                userService,
                userMaxService,
                messageSource,
                rankService
        );
    }

    @Test
    void getProfile_ReturnsFormattedProfile() {
        // Given
        Long telegramId = 123456L;
        String lang = "ru";
        User user = new User();
        user.setFullName("Denis Kaydunov");
        user.setRemindTime(LocalTime.of(13, 0));

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(13663);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PULL_UP)).thenReturn(2009);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.SQUAT)).thenReturn(2293);

        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PULL_UP)).thenReturn(15);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.SQUAT)).thenReturn(50);
        when(rankService.getRankTitle(user)).thenReturn("-");

        // When
        String profile = userProfileService.getProfile(telegramId, lang);
        System.out.println(profile);

        // Then
        assertTrue(profile.contains("📝 Имя: Denis Kaydunov"));
        assertTrue(profile.contains("⏰ Время тренировки: 13:00"));
        assertTrue(profile.contains("отжиманий: 13 663/0"));
        assertTrue(profile.contains("подтягиваний: 2 009/15"));
        assertTrue(profile.contains("приседаний: 2 293/50"));
        assertTrue(profile.contains("📊")); // статус
    }

    @Test
    void getProfile_ShowsTopRankTitle_WhenPresent() {
        // Given
        Long telegramId = 123456L;
        String lang = "ru";
        User user = new User();
        user.setFullName("Ranked User");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(0);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PULL_UP)).thenReturn(0);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.SQUAT)).thenReturn(0);

        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PULL_UP)).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.SQUAT)).thenReturn(0);

        // stub rank service to return some rank title
        when(rankService.getRankTitle(user)).thenReturn("Джон Уик");

        // When
        String profile = userProfileService.getProfile(telegramId, lang);

        // Then
        assertTrue(profile.contains("⚔  Ранг: Джон Уик"));
    }
}
