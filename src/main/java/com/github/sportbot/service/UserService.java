package com.github.sportbot.service;

import com.github.sportbot.config.SupportedLanguagesProvider;
import com.github.sportbot.dto.AchievementTrigger;
import com.github.sportbot.dto.RegistrationRequest;
import com.github.sportbot.dto.UpdateLanguageRequest;
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

    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserMapper userMapper;
    private final UnifiedAchievementService unifiedAchievementService;
    private final SupportedLanguagesProvider languagesProvider;

    @Transactional
    public UserResponse registerUser(RegistrationRequest request) {
        userRepository.findByTelegramId(request.telegramId())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException();
                });
        User user = userMapper.toEntity(request);
        Locale locale = languagesProvider.getLocale(user.getLanguage());
        user.setLanguage(locale.getLanguage());
        user = userRepository.save(user);

        if (request.referrerTelegramId() != null) {
            try {
                userRepository.findByTelegramId(request.referrerTelegramId())
                    .ifPresent(referrer -> {
                        AchievementTrigger trigger = AchievementTrigger.builder()
                                .user(referrer)
                                .type(AchievementTrigger.TriggerType.REFERRAL_REGISTERED)
                                .build();
                        unifiedAchievementService.checkAchievements(trigger);
                    });
            } catch (Exception e) {
                log.error("Failed to check referral milestones for referrer telegram ID: {}, but user registration will proceed",
                        request.referrerTelegramId(), e);
            }
        }

        String message = getMessage(USER_REGISTERED, locale);
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
        Locale locale = getUserLocale(user);
        String message;

        if (Boolean.FALSE.equals(user.getIsSubscribed())) {
            message = getMessage("unsubscribe.user.false", locale);
        } else {
            message = getMessage("unsubscribe.user.true", locale);
            user.setIsSubscribed(false);
            userRepository.save(user);
        }

        return message;
    }

    private String getMessage(String messageKey, Locale locale) {
        return messageSource.getMessage(messageKey, null, locale);
    }

    public Locale getUserLocale(User user){
        return languagesProvider.getLocale(user.getLanguage());
    }

    @Transactional
    public String updateUserLanguage(Long telegramId, UpdateLanguageRequest request) {
        User user = getUserByTelegramId(telegramId);

        // Validate and get locale
        Locale newLocale = languagesProvider.getLocale(request.language());

        user.setLanguage(newLocale.getLanguage());
        userRepository.save(user);

        log.info("Updated language for user {} to {}", telegramId, newLocale.getLanguage());

        // Return localized message in the NEW language
        return getMessage("language.changed", newLocale);
    }
}
