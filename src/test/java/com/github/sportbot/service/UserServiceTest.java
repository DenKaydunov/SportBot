package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import java.time.LocalTime;
import java.util.Locale;
import java.util.Optional;

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
                23456789,
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
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.findByTelegramId(request.telegramId())).thenReturn(Optional.empty());
        when(userMapper.toEntity(request)).thenReturn(mappedUser);
        when(userRepository.save(mappedUser)).thenReturn(mappedUser);
        when(messageSource.getMessage("user.registered", null, Locale.forLanguageTag("ru-RU")))
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
        verify(userRepository).save(mappedUser);
        verify(messageSource).getMessage("user.registered", null, Locale.forLanguageTag("ru-RU"));
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
}
