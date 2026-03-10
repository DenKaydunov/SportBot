--liquibase formatted sql

--changeset deniskaydunov:8
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS current_streak    INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS best_streak       INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS last_workout_date DATE;

COMMENT ON COLUMN users.current_streak IS 'Текущая серия дней подряд с тренировками';
COMMENT ON COLUMN users.best_streak IS 'Лучшая серия дней подряд с тренировками';
COMMENT ON COLUMN users.last_workout_date IS 'Дата последней тренировки';

