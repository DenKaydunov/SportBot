package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private RegistrationRequest request;
    private User existingUser;

    @BeforeEach
    void setUp() {
        request = new RegistrationRequest(
                123456,
                "sendPulse123",
                true,
                "John Doe",
                null,
                LocalDateTime.now().toLocalTime()
        );

        existingUser = User.builder()
                .id(1)
                .telegramId(123456)
                .fullName("Existing User")
                .build();
    }

    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.findByTelegramId(request.telegramId())).thenReturn(Optional.empty());

        // When
        userService.registerUser(request);

        // Then
        verify(userRepository).findByTelegramId(123456);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UserAlreadyExists_ThrowsException() {
        // Given
        when(userRepository.findByTelegramId(request.telegramId())).thenReturn(Optional.of(existingUser));

        // When & Then
        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(request));

        verify(userRepository).findByTelegramId(123456);
        verify(userRepository, never()).save(any(User.class));
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
