package com.github.sportbot.repository;

/**
 * Проекция для маппинга результата SQL-запроса.
 * Имена методов get... должны совпадать с алиасами в SELECT.
 */
public interface CompetitorProjection {
    Integer getPosition();
    Integer getUserId();
    String getFullName();
    Long getTotal();
}