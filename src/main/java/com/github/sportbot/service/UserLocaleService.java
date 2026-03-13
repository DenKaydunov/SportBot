package com.github.sportbot.service;

import com.github.sportbot.model.User;
import com.github.sportbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserLocaleService {
    private final UserRepository userRepository;

    public Locale getLocaleByTelegramId(Long telegramId){
        return userRepository.findByTelegramId(telegramId)
                .map(user -> Locale.forLanguageTag(user.getLanguage()))
                .orElse(Locale.forLanguageTag("ru"));
    }

    public String getUserLang(Long telegramId) {
        return userRepository.findByTelegramId(telegramId)
                .map(User::getLanguage)
                .orElse("ru");
    }

    public Locale getUserLocale(User user){
        String lang = user.getLanguage();
        if (lang == null || lang.isBlank()){
            lang = "ru";
        }
        return Locale.forLanguageTag(lang);
    }
}
