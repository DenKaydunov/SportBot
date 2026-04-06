package com.github.sportbot.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SupportedLanguagesProviderTest {

    private SupportedLanguagesProvider languagesProvider;

    @BeforeEach
    void setUp() {
        languagesProvider = new SupportedLanguagesProvider();
        languagesProvider.init();
    }

    @Test
    void init_LoadsLanguagesFromFiles() {
        // Then
        Set<String> supportedLanguages = languagesProvider.getSupportedLanguages();
        assertNotNull(supportedLanguages);
        assertFalse(supportedLanguages.isEmpty());

        // Should contain at least the languages we have files for
        assertTrue(supportedLanguages.contains("ru"));
        assertTrue(supportedLanguages.contains("en"));
        assertTrue(supportedLanguages.contains("uk"));
    }

    @Test
    void getLocale_ValidLanguage_ReturnsCorrectLocale() {
        // Then
        assertEquals(Locale.forLanguageTag("ru"), languagesProvider.getLocale("ru"));
        assertEquals(Locale.forLanguageTag("en"), languagesProvider.getLocale("en"));
        assertEquals(Locale.forLanguageTag("uk"), languagesProvider.getLocale("uk"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"invalid", "fr", "de"})
    void getLocale_InvalidOrNullOrEmptyLanguage_ReturnsDefaultLocale(String language) {
        // Then
        assertEquals(Locale.forLanguageTag("en"), languagesProvider.getLocale(language));
    }
}
