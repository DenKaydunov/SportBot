package com.github.sportbot.config;

import com.github.sportbot.service.UserLocaleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class UserLocaleInterceptor implements HandlerInterceptor {

    private final UserLocaleService userLocaleService;
    private final LocaleResolver localeResolver;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String telegramIdParam = request.getParameter("telegramId");
        if (telegramIdParam != null){
            try {
                Long telegramId = Long.parseLong(telegramIdParam);
                String lang = userLocaleService.getUserLang(telegramId);
                Locale locale = Locale.forLanguageTag(lang);
                localeResolver.setLocale(request, response, locale);
            } catch (NumberFormatException ignored) {
            }
        }
        return true;
    }
}
