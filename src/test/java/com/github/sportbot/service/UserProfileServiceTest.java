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
        Locale.setDefault(Locale.forLanguageTag("ru"));
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
        user.setBalanceTon(10);
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
        when(rankService.calculateTotalXP(user)).thenReturn(5000.8);
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Стрик: 5 дней подряд (рекорд: 10 дней)");

        // When
        String profile = userProfileService.getProfile(telegramId);
        System.out.println(profile);

        // Then
        assertTrue(profile.contains("📝 Имя: Denis Kaydunov"));
        assertTrue(profile.contains("📈 Возраст: 30"));
        assertTrue(profile.contains("📭 Пол: мужчина"));
        assertTrue(profile.contains("🌐 Язык: русский"));
        assertTrue(profile.contains("💵 Баланс: 10"));
        assertTrue(profile.contains("⏰ Время тренировки: 13:00"));
        assertTrue(profile.contains("отжиманий: 13 663/0"));
        assertTrue(profile.contains("подтягиваний: 2 009/15"));
        assertTrue(profile.contains("приседаний: 2 293/50"));
        assertTrue(profile.contains("пресс: 2 293/50"));
        assertTrue(profile.contains("💎 XP: 5000,8"));
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
        user.setLanguage("ru");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));

        var request = new UpdateProfileRequest(
                telegramId,
                25,
                Sex.WOMAN,
                "Updated User",
                "en"
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль обновлён")); // profile.updated message
        assertEquals(25, user.getAge());
        assertEquals(Sex.WOMAN, user.getSex());
        assertEquals("Updated User", user.getFullName());
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
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));

        var request = new UpdateProfileRequest(
                telegramId,
                null,
                null,
                null,
                "" // clear language
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль обновлён")); // profile.updated message
        assertNull(user.getLanguage()); // cleared
        assertNull(user.getAge()); // unchanged and still null
        assertNull(user.getFullName()); // unchanged and still null
        assertNull(user.getSex()); // unchanged and still null
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_OnlyUpdatesProvidedFields() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Original Name");
        user.setAge(25);
        user.setSex(Sex.MAN);
        user.setLanguage("ru");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));

        // Only update age, leave other fields untouched
        var request = new UpdateProfileRequest(
                telegramId,
                30,
                null,
                null,
                null
        );

        userProfileService.updateProfile(request);

        assertEquals(30, user.getAge()); // updated
        assertEquals("Original Name", user.getFullName()); // unchanged
        assertEquals(Sex.MAN, user.getSex()); // unchanged
        assertEquals("ru", user.getLanguage()); // unchanged
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_NormalizesLanguageToLowercase() {
        Long telegramId = 123456L;
        User user = new User();
        user.setLanguage("ru");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));

        var request = new UpdateProfileRequest(
                telegramId,
                null,
                null,
                null,
                "EN" // uppercase language code
        );

        userProfileService.updateProfile(request);

        assertEquals("en", user.getLanguage()); // normalized to lowercase
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_TrimsWhitespaceFromLanguage() {
        Long telegramId = 123456L;
        User user = new User();
        user.setLanguage("ru");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));

        var request = new UpdateProfileRequest(
                telegramId,
                null,
                null,
                null,
                "  en  " // language with whitespace
        );

        userProfileService.updateProfile(request);

        assertEquals("en", user.getLanguage()); // trimmed and normalized
        verify(userRepository).save(user);
    }

    @Test
    void getProfile_ShowsUnknownValueForNullFields() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Test User");
        user.setAge(null);
        user.setSex(null);
        user.setLanguage(null);
        user.setRemindTime(null);

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));
        when(exerciseService.getTotalReps(user, "push_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "pull_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "squat")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "abs")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "push_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "pull_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "squat")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "abs")).thenReturn(0);
        when(rankService.getRankTitle(eq(user), any(Locale.class))).thenReturn("-");
        when(rankService.calculateTotalXP(user)).thenReturn(0.0);
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Стрик: 0 дней");

        String profile = userProfileService.getProfile(telegramId);

        assertTrue(profile.contains("не указан")); // unknown value for null fields
    }

    @Test
    void getProfile_DisplaysEnglishLocalizationCorrectly() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("John Doe");
        user.setLanguage("en");
        user.setAge(30);
        user.setSex(Sex.MAN);
        user.setBalanceTon(10);

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("en"));
        when(exerciseService.getTotalReps(user, "push_up")).thenReturn(1000);
        when(exerciseService.getTotalReps(user, "pull_up")).thenReturn(500);
        when(exerciseService.getTotalReps(user, "squat")).thenReturn(750);
        when(exerciseService.getTotalReps(user, "abs")).thenReturn(600);
        when(userMaxService.getLastMaxByExerciseCode(user, "push_up")).thenReturn(50);
        when(userMaxService.getLastMaxByExerciseCode(user, "pull_up")).thenReturn(20);
        when(userMaxService.getLastMaxByExerciseCode(user, "squat")).thenReturn(100);
        when(userMaxService.getLastMaxByExerciseCode(user, "abs")).thenReturn(40);
        when(rankService.getRankTitle(eq(user), any(Locale.class))).thenReturn("Warrior");
        when(rankService.calculateTotalXP(user)).thenReturn(2500.5);
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Streak: 10 days");

        String profile = userProfileService.getProfile(telegramId);

        assertTrue(profile.contains("John Doe"));
        assertTrue(profile.contains("1,000")); // English number formatting
        assertTrue(profile.contains("2500")); // XP display
    }

    @Test
    void getProfile_ShowsBalanceAndXP() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Test User");
        user.setBalanceTon(50);
        user.setLanguage("ru");

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(userService.getUserLocale(user)).thenReturn(Locale.forLanguageTag("ru"));
        when(exerciseService.getTotalReps(user, "push_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "pull_up")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "squat")).thenReturn(0);
        when(exerciseService.getTotalReps(user, "abs")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "push_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "pull_up")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "squat")).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, "abs")).thenReturn(0);
        when(rankService.getRankTitle(eq(user), any(Locale.class))).thenReturn("-");
        when(rankService.calculateTotalXP(user)).thenReturn(1234.5);
        when(streakService.getStreakInfo(user)).thenReturn("🔥 Стрик: 0 дней");

        String profile = userProfileService.getProfile(telegramId);

        assertTrue(profile.contains("50")); // balance
        assertTrue(profile.contains("1234")); // XP
    }
}
