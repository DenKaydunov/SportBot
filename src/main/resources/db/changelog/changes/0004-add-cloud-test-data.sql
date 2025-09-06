--liquibase formatted sql

--changeset alexmazharouski:1 dbms:postgresql

-- =========================================
-- ⚙️ 1. Генерация 20 тестовых пользователей
-- =========================================
INSERT INTO users (telegram_id, full_name, is_subscribed, created_at, updated_at, remind_time)
SELECT
    1000000 + x                                                    AS telegram_id,
    'Test User ' || x                                              AS full_name,
    TRUE                                                           AS is_subscribed,
    CURRENT_TIMESTAMP                                              AS created_at,
    CURRENT_TIMESTAMP                                              AS updated_at,
    (time '00:00' + ((8 + (x % 12)) * interval '1 hour'))::time    AS remind_time
FROM generate_series(1, 20) AS x;

-- =========================================
-- ⚙️ 3. Генерация истории тренировок для отжиманий (pushup)
-- =========================================
INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 20) + 1,
    1,
    20 + (random() * 50)::int,
    CURRENT_DATE - ((random() * 7)::int) * interval '1 day'
FROM generate_series(1, 60) AS x;

-- =========================================
-- ⚙️ 4. Генерация истории тренировок для приседаний (squat)
-- =========================================
INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 20) + 1,
    2,
    15 + (random() * 80)::int,
    CURRENT_DATE - ((random() * 10)::int) * interval '1 day'
FROM generate_series(1, 80) AS x;

-- =========================================
-- ⚙️ 5. Генерация истории тренировок для планки (plank)
-- =========================================
INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 20) + 1,
    3,
    30 + (random() * 120)::int,
    CURRENT_DATE - ((random() * 5)::int) * interval '1 day'
FROM generate_series(1, 40) AS x;

-- =========================================
-- ⚙️ 6. Генерация максимальных результатов
-- =========================================
INSERT INTO user_max_history (user_id, exercise_type_id, max_value, date)
SELECT x, 1, 25 + (random() * 60)::int,  CURRENT_DATE - ((random() * 30)::int) * interval '1 day'
FROM generate_series(1, 20) AS x
UNION ALL
SELECT x, 2, 20 + (random() * 100)::int, CURRENT_DATE - ((random() * 30)::int) * interval '1 day'
FROM generate_series(1, 20) AS x
UNION ALL
SELECT x, 3, 40 + (random() * 150)::int, CURRENT_DATE - ((random() * 30)::int) * interval '1 day'
FROM generate_series(1, 20) AS x;

-- =========================================
-- ⚙️ 7. Дополнительные записи для топ-пользователей (больше активности)
-- =========================================
INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 5) + 1,
    (x % 3) + 1,
    30 + (random() * 70)::int,
    CURRENT_DATE - ((random() * 3)::int) * interval '1 day'
FROM generate_series(1, 50) AS x;

INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 5) + 6,
    (x % 3) + 1,
    20 + (random() * 40)::int,
    CURRENT_DATE - ((random() * 7)::int) * interval '1 day'
FROM generate_series(1, 30) AS x;

INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 5) + 11,
    (x % 3) + 1,
    10 + (random() * 25)::int,
    CURRENT_DATE - ((random() * 14)::int) * interval '1 day'
FROM generate_series(1, 20) AS x;

INSERT INTO exercise_record (user_id, exercise_type_id, count, date)
SELECT
    (x % 5) + 16,
    (x % 3) + 1,
    5 + (random() * 15)::int,
    CURRENT_DATE - ((random() * 21)::int) * interval '1 day'
FROM generate_series(1, 10) AS x;
