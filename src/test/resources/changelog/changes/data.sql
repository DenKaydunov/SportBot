--liquibase formatted sql

--changeset deniskaydunov:1

-- =========================================
-- ⚙️ 1. Генерация 20 тестовых пользователей
-- =========================================
INSERT INTO users (telegram_id, is_subscribed, created_at, updated_at)
SELECT 
    1000000 + generate_series(1, 20) as telegram_id,
    TRUE as is_subscribed,
    NOW() as created_at,
    NOW() as updated_at;

-- =========================================
-- ⚙️ 2. Генерация профилей для всех пользователей
-- =========================================
INSERT INTO user_profiles (user_id, full_name, remind_time)
SELECT 
    generate_series(1, 20) as user_id,
    'Test User ' || generate_series(1, 20) as full_name,
    ((8 + (generate_series(1, 20) % 12))::text || ':00:00')::time as remind_time;

-- =========================================
-- ⚙️ 3. Генерация истории тренировок для отжиманий (pushup)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 20) % 20) + 1 as user_id,
    1 as exercise_type_id,
    (20 + (random() * 50)::integer) as count,
    CURRENT_DATE - (random() * 7)::integer as date
FROM generate_series(1, 60); -- 60 записей (в среднем 3 на пользователя)

-- =========================================
-- ⚙️ 4. Генерация истории тренировок для приседаний (squat)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 20) % 20) + 1 as user_id,
    2 as exercise_type_id,
    (15 + (random() * 80)::integer) as count,
    CURRENT_DATE - (random() * 10)::integer as date
FROM generate_series(1, 80); -- 80 записей (в среднем 4 на пользователя)

-- =========================================
-- ⚙️ 5. Генерация истории тренировок для планки (plank)
-- =========================================
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 20) % 20) + 1 as user_id,
    3 as exercise_type_id,
    (30 + (random() * 120)::integer) as count,
    CURRENT_DATE - (random() * 5)::integer as date
FROM generate_series(1, 40); -- 40 записей (в среднем 2 на пользователя)

-- =========================================
-- ⚙️ 6. Генерация максимальных результатов
-- =========================================
INSERT INTO user_max_history (user_id, exercise_type_id, max_value, date)
SELECT 
    generate_series(1, 20) as user_id,
    1 as exercise_type_id,
    (25 + (random() * 60)::integer) as max_value,
    CURRENT_DATE - (random() * 30)::integer as date
UNION ALL
SELECT 
    generate_series(1, 20) as user_id,
    2 as exercise_type_id,
    (20 + (random() * 100)::integer) as max_value,
    CURRENT_DATE - (random() * 30)::integer as date
UNION ALL
SELECT 
    generate_series(1, 20) as user_id,
    3 as exercise_type_id,
    (40 + (random() * 150)::integer) as max_value,
    CURRENT_DATE - (random() * 30)::integer as date;

-- =========================================
-- ⚙️ 7. Дополнительные записи для топ-пользователей (больше активности)
-- =========================================
-- Пользователи 1-5 делают больше упражнений
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 50) % 5) + 1 as user_id,
    (generate_series(1, 50) % 3) + 1 as exercise_type_id,
    (30 + (random() * 70)::integer) as count,
    CURRENT_DATE - (random() * 3)::integer as date;

-- Пользователи 6-10 делают среднее количество упражнений
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 30) % 5) + 6 as user_id,
    (generate_series(1, 30) % 3) + 1 as exercise_type_id,
    (20 + (random() * 40)::integer) as count,
    CURRENT_DATE - (random() * 7)::integer as date;

-- Пользователи 11-15 делают меньше упражнений
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 20) % 5) + 11 as user_id,
    (generate_series(1, 20) % 3) + 1 as exercise_type_id,
    (10 + (random() * 25)::integer) as count,
    CURRENT_DATE - (random() * 14)::integer as date;

-- Пользователи 16-20 делают очень мало упражнений
INSERT INTO workout_history (user_id, exercise_type_id, count, date)
SELECT 
    (generate_series(1, 10) % 5) + 16 as user_id,
    (generate_series(1, 10) % 3) + 1 as exercise_type_id,
    (5 + (random() * 15)::integer) as count,
    CURRENT_DATE - (random() * 21)::integer as date;

