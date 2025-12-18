package com.github.sportbot.service;

import com.github.sportbot.dto.UpdateProfileRequest;
import com.github.sportbot.model.ExerciseTypeEnum;
import com.github.sportbot.model.User;
import com.github.sportbot.model.Sex;
import com.github.sportbot.repository.UserRepository;
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
    private UserRepository userRepository;
    private MessageSource messageSource;

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
        this.userRepository = mock(UserRepository.class);

        this.userProfileService = new UserProfileService(
                exerciseService,
                userService,
                userMaxService,
                userRepository,
                messageSource
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
        user.setLanguage("ru");
        user.setAge(30);
        user.setSex(Sex.MAN);

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(13663);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.PULL_UP)).thenReturn(2009);
        when(exerciseService.getTotalReps(user, ExerciseTypeEnum.SQUAT)).thenReturn(2293);

        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PUSH_UP)).thenReturn(0);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.PULL_UP)).thenReturn(15);
        when(userMaxService.getLastMaxByExerciseCode(user, ExerciseTypeEnum.SQUAT)).thenReturn(50);

        // When
        String profile = userProfileService.getProfile(telegramId, lang);
        System.out.println(profile);

        // Then
        assertTrue(profile.contains("📝 Имя: Denis Kaydunov"));
        assertTrue(profile.contains("📈 Возраст: 30"));
        assertTrue(profile.contains("🎭 Пол: мужчина"));
        assertTrue(profile.contains("🌐 Язык: русский"));
        assertTrue(profile.contains("⏰ Время тренировки: 13:00"));
        assertTrue(profile.contains("отжиманий: 13 663/0"));
        assertTrue(profile.contains("подтягиваний: 2 009/15"));
        assertTrue(profile.contains("приседаний: 2 293/50"));
        assertTrue(profile.contains("📊")); // статус
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
                "en"
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль Test User обновлён."));
        assertTrue(user.getAge().equals(25));
        assertTrue(user.getSex() == Sex.WOMAN);
        assertTrue(user.getLanguage().equals("en"));
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_AllowsEmptyLanguageAndKeepsNulls() {
        Long telegramId = 123456L;
        User user = new User();
        user.setFullName("Empty Lang User");
        user.setLanguage("ru"); // existing

        when(userService.getUserByTelegramId(telegramId)).thenReturn(user);

        var request = new UpdateProfileRequest(
                telegramId,
                null,
                null,
                "" // clear language
        );

        String result = userProfileService.updateProfile(request);

        assertTrue(result.contains("Профиль Empty Lang User обновлён."));
        assertNull(user.getLanguage()); // cleared
        assertNull(user.getAge()); // unchanged and still null
        assertNull(user.getSex()); // unchanged and still null
        verify(userRepository).save(user);
    }
}
