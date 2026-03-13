package com.github.sportbot.service;

import com.github.sportbot.model.User;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class LocaleService {
    public Locale getUserLocale(User user){
        return Locale.forLanguageTag(user.getLanguage());
    }
}
