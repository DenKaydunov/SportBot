package com.github.sportbot.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PeriodTest {

    @Test
    void fromCode() {
        Period result = Period.fromCode("today");
        assertEquals(Period.TODAY, result);
    }

    @Test
    void getStartDate() {
        Period result = Period.fromCode("yesterday");
        assertEquals(Period.YESTERDAY, result);
        assertTrue(LocalDate.now().isAfter(result.getStartDate()));
    }
}