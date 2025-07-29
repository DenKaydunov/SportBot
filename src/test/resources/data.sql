--liquibase formatted sql
--changeset deniskaydunov:1

-- =========================================
-- ⚙️ 0. Гарантируем наличие тестового пользователя и типов упражнений
-- =========================================
INSERT INTO users (telegram_id, is_subscribed, created_at, updated_at)
SELECT 123456789, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM users WHERE telegram_id = 123456789);

INSERT INTO exercise_types (code, title)
SELECT 'push_up', 'Push Ups'
WHERE NOT EXISTS (SELECT 1 FROM exercise_types WHERE code = 'push_up');

INSERT INTO exercise_types (code, title)
SELECT 'squat', 'Squats'
WHERE NOT EXISTS (SELECT 1 FROM exercise_types WHERE code = 'squat');

INSERT INTO exercise_types (code, title)
SELECT 'plank', 'Plank'
WHERE NOT EXISTS (SELECT 1 FROM exercise_types WHERE code = 'plank');

-- =========================================
-- ⚙️ 1. Генерация 20 тестовых пользователей
-- =========================================
INSERT INTO users (telegram_id, is_subscribed, created_at, updated_at)
SELECT
    1000000 + x,
    TRUE,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM SYSTEM_RANGE(1, 20) r(x);

-- =========================================
-- ⚙️ 2. Генерация профилей для всех пользователей
-- =========================================
INSERT INTO user_profiles (user_id, full_name, remind_time)
SELECT
    u.id,
    'Test User ' || u.id,
    CAST(DATEADD('HOUR', 8 + MOD(u.id, 12), TIME '00:00:00') AS TIME)
FROM users u
WHERE NOT EXISTS (SELECT 1 FROM user_profiles p WHERE p.user_id = u.id);

-- =========================================
-- ⚙️ 3. История тренировок для отжиманий (pushup)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    (SELECT id FROM exercise_types WHERE code='push_up'),
    20 + CAST(RAND() * 50 AS INT),
    DATEADD('DAY', -CAST(RAND() * 7 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 60) r(x);

-- =========================================
-- ⚙️ 4. История тренировок для приседаний (squat)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    (SELECT id FROM exercise_types WHERE code='squat'),
    15 + CAST(RAND() * 80 AS INT),
    DATEADD('DAY', -CAST(RAND() * 10 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 80) r(x);

-- =========================================
-- ⚙️ 5. История тренировок для планки (plank)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    (SELECT id FROM exercise_types WHERE code='plank'),
    30 + CAST(RAND() * 120 AS INT),
    DATEADD('DAY', -CAST(RAND() * 5 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 40) r(x);

-- =========================================
-- ⚙️ 6. Генерация максимальных результатов
-- =========================================
INSERT INTO user_max_history (user_id, exercise_type_id, max_value, date)
SELECT
    x,
    (SELECT id FROM exercise_types WHERE code='push_up'),
    25 + CAST(RAND() * 60 AS INT),
    DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r1(x)
UNION ALL
SELECT
    x,
    (SELECT id FROM exercise_types WHERE code='squat'),
    20 + CAST(RAND() * 100 AS INT),
    DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r2(x)
UNION ALL
SELECT
    x,
    (SELECT id FROM exercise_types WHERE code='plank'),
    40 + CAST(RAND() * 150 AS INT),
    DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r3(x);

-- =========================================
-- ⚙️ 7. Дополнительные записи для топ-пользователей
-- =========================================

-- Пользователи 1–5 делают больше упражнений
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 1,
    (SELECT id FROM exercise_types WHERE code='push_up'),
    30 + CAST(RAND() * 70 AS INT),
    DATEADD('DAY', -CAST(RAND() * 3 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 50) r(x);

-- Пользователи 6–10 — средний объём
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 6,
    (SELECT id FROM exercise_types WHERE code='squat'),
    20 + CAST(RAND() * 40 AS INT),
    DATEADD('DAY', -CAST(RAND() * 7 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 30) r(x);

-- Пользователи 11–15 — меньше
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 11,
    (SELECT id FROM exercise_types WHERE code='plank'),
    10 + CAST(RAND() * 25 AS INT),
    DATEADD('DAY', -CAST(RAND() * 14 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r(x);

-- Пользователи 16–20 — очень мало
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 16,
    (SELECT id FROM exercise_types WHERE code='push_up'),
    5 + CAST(RAND() * 15 AS INT),
    DATEADD('DAY', -CAST(RAND() * 21 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 10) r(x);
