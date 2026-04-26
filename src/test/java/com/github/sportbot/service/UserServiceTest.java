package com.github.sportbot.service;

import com.github.sportbot.config.SupportedLanguagesProvider;
import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UpdateLanguageRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.mapper.UserMapper;
import com.github.sportbot.model.Sex;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageSource messageSource;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UnifiedAchievementService unifiedAchievementService;

    @Mock
    private SupportedLanguagesProvider languagesProvider;

    @InjectMocks
    private UserService userService;

    private RegistrationRequest request;
    private User existingUser;
    private User mappedUser;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                "ru",
                Sex.MAN,
                25,
                23456789L,
                LocalTime.now()
        );

        existingUser = User.builder()
                .id(1)
                .telegramId(123456L)
                .fullName("Existing User")
                .build();

        mappedUser = User.builder()
                .id(2)
                .telegramId(request.telegramId())
                .fullName(request.fullName())
                .language(request.language())
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        Locale locale = Locale.forLanguageTag("ru");
        when(userRepository.findByTelegramId(request.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(languagesProvider.getLocale("ru")).thenReturn(locale);
        when(userRepository.save(mappedUser)).thenReturn(mappedUser);
        when(messageSource.getMessage("user.registered", null, locale))
                .thenReturn("Пользователь успешно зарегистрирован");

        // When
        UserResponse response = userService.registerUser(request);

        // Then
        assertNotNull(response);
        assertEquals(mappedUser.getTelegramId(), response.telegramId());
        assertEquals(mappedUser.getFullName(), response.fullName());
        assertEquals("Пользователь успешно зарегистрирован", response.responseMessage());

        verify(userRepository).findByTelegramId(request.telegramId());
        verify(userMapper).toEntity(request);
        verify(languagesProvider).getLocale("ru");
        verify(userRepository).save(mappedUser);
        verify(messageSource).getMessage("user.registered", null, locale);
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(request.telegramId())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));

        verify(userRepository).findByTelegramId(request.telegramId());
        verify(userMapper, never()).toEntity(any());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(messageSource);
    }

    @Test
    void registerUser_WithInvalidLanguage_NormalizesToDefault() {
        // Given
        RegistrationRequest invalidRequest = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                "invalid",
                Sex.MAN,
                25,
                null,
                LocalTime.now()
        );

        User userWithInvalidLang = User.builder()
                .id(2)
                .telegramId(invalidRequest.telegramId())
                .fullName(invalidRequest.fullName())
                .language("invalid")
                .build();

        Locale defaultLocale = Locale.forLanguageTag("en");
        when(userRepository.findByTelegramId(invalidRequest.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(invalidRequest)).thenReturn(userWithInvalidLang);
        when(languagesProvider.getLocale("invalid")).thenReturn(defaultLocale);
        when(userRepository.save(any(User.class))).thenReturn(userWithInvalidLang);
        when(messageSource.getMessage("user.registered", null, defaultLocale))
                .thenReturn("User successfully registered");

        // When
        UserResponse response = userService.registerUser(invalidRequest);

        // Then
        assertNotNull(response);
        assertEquals("en", userWithInvalidLang.getLanguage()); // Language should be normalized to "en"

        verify(userRepository).findByTelegramId(invalidRequest.telegramId());
        verify(userMapper).toEntity(invalidRequest);
        verify(languagesProvider).getLocale("invalid");
        verify(userRepository).save(userWithInvalidLang);
    }

    @Test
    void registerUser_WithNullLanguage_NormalizesToDefault() {
        // Given
        RegistrationRequest nullLangRequest = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                null,
                Sex.MAN,
                25,
                null,
                LocalTime.now()
        );

        User userWithNullLang = User.builder()
                .id(2)
                .telegramId(nullLangRequest.telegramId())
                .fullName(nullLangRequest.fullName())
                .language(null)
                .build();

        Locale defaultLocale = Locale.forLanguageTag("en");
        when(userRepository.findByTelegramId(nullLangRequest.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(nullLangRequest)).thenReturn(userWithNullLang);
        when(languagesProvider.getLocale(null)).thenReturn(defaultLocale);
        when(userRepository.save(any(User.class))).thenReturn(userWithNullLang);
        when(messageSource.getMessage("user.registered", null, defaultLocale))
                .thenReturn("User successfully registered");

        // When
        UserResponse response = userService.registerUser(nullLangRequest);

        // Then
        assertNotNull(response);
        assertEquals("en", userWithNullLang.getLanguage()); // Language should be normalized to "en"

        verify(userRepository).findByTelegramId(nullLangRequest.telegramId());
        verify(userMapper).toEntity(nullLangRequest);
        verify(languagesProvider).getLocale(null);
        verify(userRepository).save(userWithNullLang);
    }

    @Test
    void getUserByTelegramId_Success() {
        // Given
        when(userRepository.findByTelegramId(123456L)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.getUserByTelegramId(123456L);

        // Then
        assertEquals(existingUser, result);
        verify(userRepository).findByTelegramId(123456L);
    }

    @Test
    void getUserByTelegramId_NotFound_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(123456L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByTelegramId(123456L));

        verify(userRepository).findByTelegramId(123456L);
    }

    @Test
    void getOrCreateUser_UserExists_ReturnsExistingUser() {
        // Given
        Long telegramId = 123456L;
        String fullName = "John Doe";
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.getOrCreateUser(telegramId, fullName);

        // Then
        assertEquals(existingUser, result);
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_UserDoesNotExist_CreatesNewUser() {
        // Given
        Long telegramId = 999999L;
        String fullName = "New User";
        User newUser = User.builder()
                .telegramId(telegramId)
                .fullName(fullName)
                .build();

        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.getOrCreateUser(telegramId, fullName);

        // Then
        assertNotNull(result);
        assertEquals(telegramId, result.getTelegramId());
        assertEquals(fullName, result.getFullName());
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getOrCreateUser_UserDoesNotExist_WithNullName_CreatesUserWithDefaultName() {
        // Given
        Long telegramId = 999999L;
        String expectedName = "User " + telegramId;
        User newUser = User.builder()
                .telegramId(telegramId)
                .fullName(expectedName)
                .build();

        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        // When
        User result = userService.getOrCreateUser(telegramId, null);

        // Then
        assertNotNull(result);
        assertEquals(telegramId, result.getTelegramId());
        assertEquals(expectedName, result.getFullName());
        verify(userRepository).findByTelegramId(telegramId);
        verify(userRepository).save(any(User.class));
    }

    @ParameterizedTest
    @MethodSource("provideLanguageTestCases")
    void getUserLocale_ReturnsExpectedLocale(String inputLanguage, String expectedLanguage) {
        // Given
        User user = User.builder().language(inputLanguage).build();
        Locale expectedLocale = Locale.forLanguageTag(expectedLanguage);
        when(languagesProvider.getLocale(inputLanguage)).thenReturn(expectedLocale);

        // When
        Locale result = userService.getUserLocale(user);

        // Then
        assertEquals(expectedLocale, result);
        verify(languagesProvider).getLocale(inputLanguage);
    }

    private static Stream<Arguments> provideLanguageTestCases() {
        return Stream.of(
            Arguments.of("ru", "ru"),      // Valid language returns itself
            Arguments.of("en", "en"),      // Valid language returns itself
            Arguments.of("uk", "uk"),      // Valid language returns itself
            Arguments.of("invalid", "en"), // Invalid language returns default
            Arguments.of(null, "en")       // Null language returns default
        );
    }

    @Test
    void registerUser_WithValidReferrer_Success() {
        // Given
        Long referrerTelegramId = 999888777L;
        RegistrationRequest requestWithReferrer = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                "ru",
                Sex.MAN,
                25,
                referrerTelegramId,
                LocalTime.now()
        );

        User referrerUser = User.builder()
                .id(10)
                .telegramId(referrerTelegramId)
                .fullName("Referrer User")
                .build();

        User userWithReferrer = User.builder()
                .id(2)
                .telegramId(requestWithReferrer.telegramId())
                .fullName(requestWithReferrer.fullName())
                .language(requestWithReferrer.language())
                .referrerTelegramId(referrerTelegramId)
                .build();

        Locale locale = Locale.forLanguageTag("ru");
        when(userRepository.findByTelegramId(requestWithReferrer.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestWithReferrer)).thenReturn(userWithReferrer);
        when(languagesProvider.getLocale("ru")).thenReturn(locale);
        when(userRepository.save(userWithReferrer)).thenReturn(userWithReferrer);
        when(userRepository.findByTelegramId(referrerTelegramId)).thenReturn(Optional.of(referrerUser));
        when(messageSource.getMessage("user.registered", null, locale))
                .thenReturn("Пользователь успешно зарегистрирован");

        // When
        UserResponse response = userService.registerUser(requestWithReferrer);

        // Then
        assertNotNull(response);
        assertEquals(userWithReferrer.getTelegramId(), response.telegramId());
        assertEquals(userWithReferrer.getFullName(), response.fullName());
        assertEquals("Пользователь успешно зарегистрирован", response.responseMessage());

        verify(userRepository).findByTelegramId(requestWithReferrer.telegramId());
        verify(userMapper).toEntity(requestWithReferrer);
        verify(languagesProvider).getLocale("ru");
        verify(userRepository).save(userWithReferrer);
        verify(userRepository).findByTelegramId(referrerTelegramId);

        // Verify that unified achievement service was called with correct trigger
        ArgumentCaptor<AchievementTrigger> triggerCaptor = ArgumentCaptor.forClass(AchievementTrigger.class);
        verify(unifiedAchievementService).checkAchievements(triggerCaptor.capture());
        AchievementTrigger capturedTrigger = triggerCaptor.getValue();
        assertEquals(referrerUser, capturedTrigger.getUser());
        assertEquals(AchievementTrigger.TriggerType.REFERRAL_REGISTERED, capturedTrigger.getType());

        verify(messageSource).getMessage("user.registered", null, locale);
    }

    @Test
    void registerUser_WithInvalidReferrer_StillSucceeds() {
        // Given
        Long referrerTelegramId = 999888777L;
        RegistrationRequest requestWithInvalidReferrer = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                "ru",
                Sex.MAN,
                25,
                referrerTelegramId,
                LocalTime.now()
        );

        User userWithReferrer = User.builder()
                .id(2)
                .telegramId(requestWithInvalidReferrer.telegramId())
                .fullName(requestWithInvalidReferrer.fullName())
                .language(requestWithInvalidReferrer.language())
                .referrerTelegramId(referrerTelegramId)
                .build();

        Locale locale = Locale.forLanguageTag("ru");
        when(userRepository.findByTelegramId(requestWithInvalidReferrer.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestWithInvalidReferrer)).thenReturn(userWithReferrer);
        when(languagesProvider.getLocale("ru")).thenReturn(locale);
        when(userRepository.save(userWithReferrer)).thenReturn(userWithReferrer);
        when(userRepository.findByTelegramId(referrerTelegramId)).thenReturn(Optional.empty());
        when(messageSource.getMessage("user.registered", null, locale))
                .thenReturn("Пользователь успешно зарегистрирован");

        // When
        UserResponse response = userService.registerUser(requestWithInvalidReferrer);

        // Then
        assertNotNull(response);
        assertEquals(userWithReferrer.getTelegramId(), response.telegramId());
        assertEquals(userWithReferrer.getFullName(), response.fullName());
        assertEquals("Пользователь успешно зарегистрирован", response.responseMessage());

        verify(userRepository).findByTelegramId(requestWithInvalidReferrer.telegramId());
        verify(userMapper).toEntity(requestWithInvalidReferrer);
        verify(languagesProvider).getLocale("ru");
        verify(userRepository).save(userWithReferrer);
        verify(userRepository).findByTelegramId(referrerTelegramId);
        verify(unifiedAchievementService, never()).checkAchievements(any(AchievementTrigger.class));
        verify(messageSource).getMessage("user.registered", null, locale);
    }

    @Test
    void registerUser_ReferralMilestoneCheckFails_RegistrationSucceeds() {
        // Given
        Long referrerTelegramId = 999888777L;
        RegistrationRequest requestWithReferrer = new RegistrationRequest(
                123456L,
                "sendPulse123",
                true,
                "John Doe",
                "ru",
                Sex.MAN,
                25,
                referrerTelegramId,
                LocalTime.now()
        );

        User referrerUser = User.builder()
                .id(10)
                .telegramId(referrerTelegramId)
                .fullName("Referrer User")
                .build();

        User userWithReferrer = User.builder()
                .id(2)
                .telegramId(requestWithReferrer.telegramId())
                .fullName(requestWithReferrer.fullName())
                .language(requestWithReferrer.language())
                .referrerTelegramId(referrerTelegramId)
                .build();

        Locale locale = Locale.forLanguageTag("ru");
        when(userRepository.findByTelegramId(requestWithReferrer.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(requestWithReferrer)).thenReturn(userWithReferrer);
        when(languagesProvider.getLocale("ru")).thenReturn(locale);
        when(userRepository.save(userWithReferrer)).thenReturn(userWithReferrer);
        when(userRepository.findByTelegramId(referrerTelegramId)).thenReturn(Optional.of(referrerUser));
        doThrow(new RuntimeException("TEST ERROR: Simulated achievement service failure for testing error handling"))
                .when(unifiedAchievementService).checkAchievements(any(AchievementTrigger.class));
        when(messageSource.getMessage("user.registered", null, locale))
                .thenReturn("Пользователь успешно зарегистрирован");

        // When
        UserResponse response = userService.registerUser(requestWithReferrer);

        // Then
        assertNotNull(response);
        assertEquals(userWithReferrer.getTelegramId(), response.telegramId());
        assertEquals(userWithReferrer.getFullName(), response.fullName());
        assertEquals("Пользователь успешно зарегистрирован", response.responseMessage());

        verify(userRepository).findByTelegramId(requestWithReferrer.telegramId());
        verify(userMapper).toEntity(requestWithReferrer);
        verify(languagesProvider).getLocale("ru");
        verify(userRepository).save(userWithReferrer);
        verify(userRepository).findByTelegramId(referrerTelegramId);

        // Verify that unified achievement service was called even though it failed
        ArgumentCaptor<AchievementTrigger> triggerCaptor = ArgumentCaptor.forClass(AchievementTrigger.class);
        verify(unifiedAchievementService).checkAchievements(triggerCaptor.capture());
        AchievementTrigger capturedTrigger = triggerCaptor.getValue();
        assertEquals(referrerUser, capturedTrigger.getUser());
        assertEquals(AchievementTrigger.TriggerType.REFERRAL_REGISTERED, capturedTrigger.getType());

        verify(messageSource).getMessage("user.registered", null, locale);
    }

    @Test
    void updateUserLanguage_Success() {
        // Given
        Long telegramId = 123456L;
        UpdateLanguageRequest request = new UpdateLanguageRequest("en");
        User user = User.builder()
                .id(1)
                .telegramId(telegramId)
                .fullName("Test User")
                .language("ru")
                .build();

        Locale newLocale = Locale.forLanguageTag("en");
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        when(languagesProvider.getLocale("en")).thenReturn(newLocale);
        when(userRepository.save(user)).thenReturn(user);
        when(messageSource.getMessage("language.changed", null, newLocale))
                .thenReturn("Language successfully changed");

        // When
        String result = userService.updateUserLanguage(telegramId, request);

        // Then
        assertEquals("Language successfully changed", result);
        assertEquals("en", user.getLanguage());
        verify(userRepository).findByTelegramId(telegramId);
        verify(languagesProvider).getLocale("en");
        verify(userRepository).save(user);
        verify(messageSource).getMessage("language.changed", null, newLocale);
    }

    @Test
    void updateUserLanguage_UserNotFound_ThrowsException() {
        // Given
        Long telegramId = 999999L;
        UpdateLanguageRequest request = new UpdateLanguageRequest("en");
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUserLanguage(telegramId, request));

        verify(userRepository).findByTelegramId(telegramId);
        verify(languagesProvider, never()).getLocale(anyString());
        verify(userRepository, never()).save(any());
        verifyNoInteractions(messageSource);
    }

    @ParameterizedTest
    @MethodSource("provideLanguageUpdateTestCases")
    void updateUserLanguage_VariousLanguages_Success(String newLanguage, String expectedLanguage, String expectedMessage) {
        // Given
        Long telegramId = 123456L;
        UpdateLanguageRequest request = new UpdateLanguageRequest(newLanguage);
        User user = User.builder()
                .id(1)
                .telegramId(telegramId)
                .fullName("Test User")
                .language("ru")
                .build();

        Locale newLocale = Locale.forLanguageTag(expectedLanguage);
        when(userRepository.findByTelegramId(telegramId)).thenReturn(Optional.of(user));
        when(languagesProvider.getLocale(newLanguage)).thenReturn(newLocale);
        when(userRepository.save(user)).thenReturn(user);
        when(messageSource.getMessage("language.changed", null, newLocale))
                .thenReturn(expectedMessage);

        // When
        String result = userService.updateUserLanguage(telegramId, request);

        // Then
        assertEquals(expectedMessage, result);
        assertEquals(expectedLanguage, user.getLanguage());
        verify(userRepository).findByTelegramId(telegramId);
        verify(languagesProvider).getLocale(newLanguage);
        verify(userRepository).save(user);
        verify(messageSource).getMessage("language.changed", null, newLocale);
    }

    private static Stream<Arguments> provideLanguageUpdateTestCases() {
        return Stream.of(
                Arguments.of("ru", "ru", "Язык успешно изменен"),
                Arguments.of("en", "en", "Language successfully changed"),
                Arguments.of("uk", "uk", "Мова успішно змінена")
        );
    }
}
