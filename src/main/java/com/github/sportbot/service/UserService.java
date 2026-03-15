package com.github.sportbot.service;

import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UserResponse;
import com.github.sportbot.exception.UserAlreadyExistsException;
import com.github.sportbot.exception.UserNotFoundException;
import com.github.sportbot.mapper.UserMapper;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService{

    public static final String USER_REGISTERED = "user.registered";
    private static final Locale LOCALE = Locale.forLanguageTag("ru-RU");

    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse registerUser(RegistrationRequest request) {
        userRepository.findByTelegramId(request.telegramId())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException();
                });
        User user = userMapper.toEntity(request);
        user = userRepository.save(user);
        String message = getMessage(USER_REGISTERED, user);
        return new UserResponse(message, user.getTelegramId(), user.getFullName());
    }

    public User getUserByTelegramId(@NotNull Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public User getOrCreateUser(@NotNull Long telegramId, String fullName) {
        return userRepository.findByTelegramId(telegramId)
                .orElseGet(() -> {
                    log.info("Creating new user with telegramId: {}, fullName: {}", telegramId, fullName);
                    User newUser = User.builder()
                            .telegramId(telegramId)
                            .fullName(fullName != null ? fullName : "User " + telegramId)
                            .build();
                    return userRepository.save(newUser);
                });
    }

    public Page<UserResponse> getAllUsersPaged(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> new UserResponse(
                        "Success",
                        user.getTelegramId(),
                        user.getFullName()
                ));
    }

    public boolean isSubscribedUser(Long telegramId) {
        return userRepository.existsByTelegramIdAndIsSubscribedTrue(telegramId);
    }

    @Transactional
    public String unsubscribeUser(Long telegramId) {
        User user = getUserByTelegramId(telegramId);
        String message;

        if (Boolean.FALSE.equals(user.getIsSubscribed())) {
            message = getMessage("unsubscribe.user.false", user);
        } else {
            message = getMessage("unsubscribe.user.true", user);
            user.setIsSubscribed(false);
            userRepository.save(user);
        }

        return message;
    }

    private String getMessage(String messageKey, User user) {
        return messageSource.getMessage(messageKey, null, getUserLocale(user));
    }

    public Locale getUserLocale(User user){
        String lang = user.getLanguage();
        if (!"ru".equals(lang) && !"en".equals(lang)){
            lang = "ru";
        }
        return Locale.forLanguageTag(lang);
    }
}
