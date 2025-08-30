package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.exception.DaoException;
import com.github.sportbot.model.User;
import com.github.sportbot.model.UserProfile;
import com.github.sportbot.repository.UserProfileRepository;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void registerUser(RegistrationRequest request) {
        userRepository.findByTelegramId(request.telegramId()).orElseThrow(() -> new DaoException("User not found"));

        User user = User.builder()
                .telegramId(request.telegramId())
                .sendPulseId(request.sendPulseId())
                .isSubscribed(request.isSubscribed())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(request.fullName())
                .referrerTelegramId(request.referrerTelegramId())
                .remindTime(request.remindTime())
                .build();
        userProfileRepository.save(profile);
    }
}

