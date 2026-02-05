--liquibase formatted sql

--changeset sportbot:8
ALTER TABLE users
    ADD COLUMN current_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN best_streak INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_workout_date DATE;

COMMENT ON COLUMN users.current_streak IS 'Текущая серия дней подряд с тренировками';
COMMENT ON COLUMN users.best_streak IS 'Лучшая серия дней подряд с тренировками';
COMMENT ON COLUMN users.last_workout_date IS 'Дата последней тренировки';

