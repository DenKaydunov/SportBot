package com.github.sportbot.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class SupportedLanguagesProvider {

    private static final String DEFAULT_LANGUAGE = "en";
    private static final String MESSAGES_PATTERN = "classpath:messages/messages_*.properties";
    private static final Pattern LANGUAGE_PATTERN = Pattern.compile("messages_(\\w+)\\.properties");

    @Getter
    private Set<String> supportedLanguages;

    @PostConstruct
    public void init() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(MESSAGES_PATTERN);

            supportedLanguages = Stream.of(resources)
                    .map(Resource::getFilename)
                    .filter(Objects::nonNull)
                    .map(LANGUAGE_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .collect(Collectors.toSet());

            if (supportedLanguages.isEmpty()) {
                log.warn("No translation files found, using default language: {}", DEFAULT_LANGUAGE);
                supportedLanguages = Set.of(DEFAULT_LANGUAGE);
            }

            log.info("Supported languages loaded: {}", supportedLanguages);
        } catch (IOException e) {
            log.error("Failed to load translation files, using default language", e);
            supportedLanguages = Collections.singleton(DEFAULT_LANGUAGE);
        }
    }

    public Locale getLocale(String language) {
        return Locale.forLanguageTag(normalizeLanguage(language));
    }

    private boolean isLanguageSupported(String language) {
        return language != null && supportedLanguages.contains(language);
    }

    private String normalizeLanguage(String language) {
        return isLanguageSupported(language) ? language : DEFAULT_LANGUAGE;
    }
}
