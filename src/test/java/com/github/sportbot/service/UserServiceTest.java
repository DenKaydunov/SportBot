package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UserRegistrationResponse;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.mapper.UserMapper;
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
                123456,
                "sendPulse123",
                true,
                "John Doe",
                null,
                LocalTime.now()
        );

        existingUser = User.builder()
                .id(1)
                .telegramId(123456)
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
        UserRegistrationResponse response = userService.registerUser(request);

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
        when(userRepository.findByTelegramId(123456)).thenReturn(Optional.of(existingUser));

        // When
        User result = userService.getUserByTelegramId(123456);

        // Then
        assertEquals(existingUser, result);
        verify(userRepository).findByTelegramId(123456);
    }

    @Test
    void getUserByTelegramId_NotFound_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(123456)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getUserByTelegramId(123456));

        verify(userRepository).findByTelegramId(123456);
    }
}
