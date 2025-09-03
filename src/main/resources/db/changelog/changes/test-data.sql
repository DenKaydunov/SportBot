--liquibase formatted sql

--changeset deniskaydunov:1 context:test dbms:h2

-- =========================================
-- ⚙️ 1. Генерация 20 тестовых пользователей
-- =========================================
INSERT INTO users (telegram_id, full_name, is_subscribed, created_at, updated_at, remind_time)
SELECT
    1000000 + x                                        AS telegram_id,
    'Test User ' || x                                  AS full_name,
    TRUE                                               AS is_subscribed,
    CURRENT_TIMESTAMP                                  AS created_at,
    CURRENT_TIMESTAMP                                  AS updated_at,
    CAST(DATEADD('HOUR', 8 + MOD(x, 12), TIMESTAMP '1970-01-01 00:00:00') AS TIME) AS remind_time
FROM SYSTEM_RANGE(1, 20) r(x);
-- =========================================
-- ⚙️ 3. Генерация истории тренировок для отжиманий (pushup)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    1,
    20 + CAST(RAND() * 50 AS INT),
    DATEADD('DAY', -CAST(RAND() * 7 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 60) r(x);

-- =========================================
-- ⚙️ 4. Генерация истории тренировок для приседаний (squat)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    2,
    15 + CAST(RAND() * 80 AS INT),
    DATEADD('DAY', -CAST(RAND() * 10 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 80) r(x);

-- =========================================
-- ⚙️ 5. Генерация истории тренировок для планки (plank)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 20) + 1,
    3,
    30 + CAST(RAND() * 120 AS INT),
    DATEADD('DAY', -CAST(RAND() * 5 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 40) r(x);

-- =========================================
-- ⚙️ 6. Генерация максимальных результатов
-- =========================================
INSERT INTO user_max_history (user_id, exercise_type_id, max_value, date)
SELECT x, 1, 25 + CAST(RAND() * 60 AS INT),  DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r(x)
UNION ALL
SELECT x, 2, 20 + CAST(RAND() * 100 AS INT), DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r2(x)
UNION ALL
SELECT x, 3, 40 + CAST(RAND() * 150 AS INT), DATEADD('DAY', -CAST(RAND() * 30 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r3(x);

-- =========================================
-- ⚙️ 7. Дополнительные записи для топ-пользователей (больше активности)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 1,
    MOD(x, 3) + 1,
    30 + CAST(RAND() * 70 AS INT),
    DATEADD('DAY', -CAST(RAND() * 3 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 50) r(x);

INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 6,
    MOD(x, 3) + 1,
    20 + CAST(RAND() * 40 AS INT),
    DATEADD('DAY', -CAST(RAND() * 7 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 30) r(x);

INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 11,
    MOD(x, 3) + 1,
    10 + CAST(RAND() * 25 AS INT),
    DATEADD('DAY', -CAST(RAND() * 14 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 20) r(x);

INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT
    MOD(x, 5) + 16,
    MOD(x, 3) + 1,
    5 + CAST(RAND() * 15 AS INT),
    DATEADD('DAY', -CAST(RAND() * 21 AS INT), CURRENT_DATE)
FROM SYSTEM_RANGE(1, 10) r(x);

