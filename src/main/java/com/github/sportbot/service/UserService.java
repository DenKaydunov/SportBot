package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void registerUser(RegistrationRequest request) {
        userRepository.findByTelegramId(request.telegramId())
                .ifPresent(user -> {throw new UserAlreadyExistsException();});

        User user = User.builder()
                .fullName(request.fullName())
                .telegramId(request.telegramId())
                .referrerTelegramId(request.referrerTelegramId())
                .sendPulseId(request.sendPulseId())
                .isSubscribed(request.isSubscribed())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .remindTime(request.remindTime())
                .build();
        userRepository.save(user);
    }

    public User getUserByTelegramId(Integer telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }
}
