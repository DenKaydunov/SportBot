package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UserRegistrationResponse;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.mapper.UserMapper;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserMapper userMapper;

    @Transactional
    public UserRegistrationResponse registerUser(RegistrationRequest request) {
        userRepository.findByTelegramId(request.telegramId())
                .ifPresent(user -> {throw new UserAlreadyExistsException();});
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        String message = getLocalizedResponseMessage();
        return new UserRegistrationResponse(message, user.getTelegramId(), user.getFullName());
    }

    private String getLocalizedResponseMessage() {
        return messageSource.getMessage(
                "user.registered",
                null,
                Locale.forLanguageTag("ru-RU")
        );
    }

    public User getUserByTelegramId(@NotNull Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }
}
