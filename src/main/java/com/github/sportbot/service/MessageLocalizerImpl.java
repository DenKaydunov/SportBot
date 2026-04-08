package com.github.sportbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Primary
@RequiredArgsConstructor
public class MessageLocalizerImpl implements MessageLocalizer {

    private final MessageSource messageSource;

    @Override
    public String localize(String messageKey, Object[] context, Locale locale) {
        return messageSource.getMessage(messageKey, context, locale);
    }
}
