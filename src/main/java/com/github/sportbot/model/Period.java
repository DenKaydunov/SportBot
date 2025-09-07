package com.github.sportbot.model;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.function.Supplier;

@Getter
public enum Period {
    TODAY("today", "сегодня", "За сегодня", LocalDate::now),
    YESTERDAY("yesterday", "вчера", "За вчера", () -> LocalDate.now().minusDays(1)),
    WEEK("week", "неделя", "За неделю", () -> LocalDate.now().minusWeeks(1)),
    MONTH("month", "месяц", "За месяц", () -> LocalDate.now().minusMonths(1)),
    ALL("all", "все", "За все время", () -> null);

    private final String englishCode;
    private final String russianCode;
    private final String displayName;
    private final Supplier<LocalDate> startDateSupplier;

    Period(String englishCode, String russianCode, String displayName, Supplier<LocalDate> startDateSupplier) {
        this.englishCode = englishCode;
        this.russianCode = russianCode;
        this.displayName = displayName;
        this.startDateSupplier = startDateSupplier;
    }

    public LocalDate getStartDate() {
        return startDateSupplier.get();
    }

    public static Period fromCode(String code) {
        return Arrays.stream(values())
                .filter(period -> period.englishCode.equals(code))
                .findFirst()
                .orElse(Period.ALL);
    }
}
