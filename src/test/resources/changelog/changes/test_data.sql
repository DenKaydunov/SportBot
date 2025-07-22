--liquibase formatted sql

--changeset deniskaydunov:1

-- =========================================
-- ⚙️ 1. Завести тестового пользователя
--    (телеграм‑id берём «123456789» как пример)
-- =========================================
INSERT INTO users (telegram_id, is_subscribed, created_at, updated_at)
VALUES
    ( 123456789, TRUE, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- ⚙️ 2. Добавить профиль к пользователю
-- =========================================
INSERT INTO user_profiles ( user_id, full_name, remind_time)
VALUES
    ( 1, 'Юра', '09:00:00')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- ⚙️ 3. Пример истории тренировок
-- (два подхода по 25 отжиманий за сегодня)
-- =========================================
INSERT INTO workout_history ( user_id, exercise_type_id, count, date)
VALUES
    ( 1, 1, 25, CURRENT_DATE),
    ( 1, 1, 25, CURRENT_DATE)
ON CONFLICT (id) DO NOTHING;

