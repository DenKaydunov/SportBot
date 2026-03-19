package com.github.sportbot.service;

import com.github.sportbot.dto.UpdateProfileRequest;
import com.github.sportbot.model.User;
import com.github.sportbot.model.Sex;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.time.LocalTime;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileServiceTest {

    private UserService userService;
    private ExerciseService exerciseService;
    private UserMaxService userMaxService;
    private UserRepository userRepository;
    private MessageSource messageSource;
    private RankService rankService;
    private StreakService streakService;

    private UserProfileService userProfileService;

    @BeforeEach
    void setUp() {
        ResourceBundleMessageSource realMessageSource = new ResourceBundleMessageSource();
        Locale.setDefault(new Locale("ru"));
        realMessageSource.setBasename("messages/messages");
        realMessageSource.setDefaultEncoding("UTF-8");
        this.messageSource = realMessageSource;

        this.userService = mock(UserService.class);
        this.exerciseService = mock(ExerciseService.class);
        this.userMaxService = mock(UserMaxService.class);
        this.rankService = mock(RankService.class);
        this.streakService = mock(StreakService.class);
        this.userRepository = mock(UserRepository.class);

        this.userProfileService = new UserProfileService(
                exerciseService,
                userService,
                userMaxService,
                userRepository,
                messageSource,
                rankService,
                streakService
        );
    }

    @Test
    void getProfile_ReturnsFormattedProfile() {
        // Given
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Denis Kaydunov");
        user.setRemindTime(LocalTime.of(13, 0));
        user.setLanguage("ru");
        user.setAge(30);
        user.setSex(Sex.MAN);

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, "push_up")).thenReturn(13663);
        when(exerciseService.getTotalReps(user, "pull_up")).thenReturn(2009);
        when(exerciseService.getTotalReps(user, "squat")).thenReturn(2293);
        when(exerciseService.getTotalReps(user, "abs")).thenReturn(2293);

        when(userMaxService.getLastMaxByExerciseCode(user, "push_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "pull_up")).thenReturn(15);
        when(userMaxService.getLastMaxByExerciseCode(user, "squat")).thenReturn(50);
        when(userMaxService.getLastMaxByExerciseCode(user, "abs")).thenReturn(50);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));
        when(rankService.getRankTitle(eq(user), any(Locale.class))).thenReturn("-");
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Стрик: 5 дней подряд (рекорд: 10 дней)");

        // When
        String profile = userProfileService.getProfile(telegramId);
        System.out.println(profile);

        // Then
        assertTrue(profile.contains("📝 Имя: Denis Kaydunov"));
        assertTrue(profile.contains("📈 Возраст: 30"));
        assertTrue(profile.contains("📭 Пол: мужчина"));
        assertTrue(profile.contains("🌐 Язык: русский"));
        assertTrue(profile.contains("⏰ Время тренировки: 13:00"));
        assertTrue(profile.contains("отжиманий: 13 663/0"));
        assertTrue(profile.contains("подтягиваний: 2 009/15"));
        assertTrue(profile.contains("приседаний: 2 293/50"));
        assertTrue(profile.contains("пресс: 2 293/50"));
        assertTrue(profile.contains("📊")); // статус




        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("en"));
        profile = userProfileService.getProfile(telegramId);

        assertTrue(profile.contains("push-ups: 13,663/0"));
        assertTrue(profile.contains("pull-ups: 2,009/15"));
        assertTrue(profile.contains("squats: 2,293/50"));
        assertTrue(profile.contains("abs: 2,293/50"));


    }

    @Test
    void getProfile_ShowsTopRankTitle_WhenPresent() {
        // Given
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Ranked User");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, "push_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "pull_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "squat")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "abs")).thenReturn(0);

        when(userMaxService.getLastMaxByExerciseCode(user, "push_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "pull_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "squat")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "abs")).thenReturn(0);

        // stub rank service to return some rank title
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));
        when(rankService.getRankTitle(eq(user), any(Locale.class))).thenReturn("Джон Уик");
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Стрик: 100 дней подряд (новый рекорд! 🎉)");

        // When
        String profile = userProfileService.getProfile(telegramId);

        // Then
        assertTrue(profile.contains("0"));
    }

    @Test
    void updateProfile_UpdatesFieldsAndReturnsMessage() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Test User");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);

        var request = new UpdateProfileRequest(
                telegramId,
                25,
                Sex.WOMAN,
                "Test User",
                "en"
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль Test User обновлён."));
        assertEquals(25, user.getAge());
        assertEquals(Sex.WOMAN, user.getSex());
        assertEquals("Test User", user.getFullName());
        assertEquals("en", user.getLanguage());
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_AllowsEmptyLanguageAndKeepsNulls() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName(null);
        user.setLanguage("ru"); // existing

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);

        var request = new UpdateProfileRequest(
                telegramId,
                null,
                null,
                null,
                "" // clear language
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль null обновлён."));
        assertNull(user.getLanguage()); // cleared
        assertNull(user.getAge()); // unchanged and still null
        assertNull(user.getFullName()); // unchanged and still null
        assertNull(user.getSex()); // unchanged and still null
        verify(userRepository).save(user);
    }
}
