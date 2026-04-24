--liquibase formatted sql

--changeset unified-achievements:1
-- Подготовка: очистка старых данных и удаление deprecated колонок
DELETE FROM user_achievements;
DELETE FROM achievement_definitions;
ALTER TABLE achievement_definitions DROP COLUMN IF EXISTS title_key;
ALTER TABLE achievement_definitions DROP COLUMN IF EXISTS description_key;

--changeset unified-achievements:2
-- Создание таблицы локализации достижений
CREATE TABLE achievements (
    id BIGSERIAL PRIMARY KEY,
    achievement_definition_id BIGINT NOT NULL,
    language VARCHAR(5) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_achievements_definition
        FOREIGN KEY (achievement_definition_id)
        REFERENCES achievement_definitions(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_achievements_definition_language
        UNIQUE(achievement_definition_id, language)
);

CREATE INDEX idx_achievements_definition ON achievements(achievement_definition_id);
CREATE INDEX idx_achievements_language ON achievements(language);

--changeset unified-achievements:3
-- Вставка всех 80 определений достижений
INSERT INTO achievement_definitions
(code, category, emoji, exercise_type_id, target_value, reward_ton, sort_order, is_legendary, is_active)
VALUES
-- TOTAL_REPS: Push-ups (7 tiers)
('PUSHUP_TOTAL_100', 'TOTAL_REPS', '💪', 1, 100, 1, 10, false, true),
('PUSHUP_TOTAL_500', 'TOTAL_REPS', '💪', 1, 500, 2, 11, false, true),
('PUSHUP_TOTAL_1000', 'TOTAL_REPS', '💪', 1, 1000, 3, 12, false, true),
('PUSHUP_TOTAL_5000', 'TOTAL_REPS', '💪', 1, 5000, 4, 13, false, true),
('PUSHUP_TOTAL_10000', 'TOTAL_REPS', '💪', 1, 10000, 5, 14, false, true),
('PUSHUP_TOTAL_50000', 'TOTAL_REPS', '💪', 1, 50000, 10, 15, false, true),
('PUSHUP_TOTAL_100000', 'TOTAL_REPS', '💪', 1, 100000, 25, 16, true, true),

-- TOTAL_REPS: Pull-ups (7 tiers)
('PULLUP_TOTAL_100', 'TOTAL_REPS', '🦾', 2, 100, 2, 20, false, true),
('PULLUP_TOTAL_500', 'TOTAL_REPS', '🦾', 2, 500, 4, 21, false, true),
('PULLUP_TOTAL_1000', 'TOTAL_REPS', '🦾', 2, 1000, 6, 22, false, true),
('PULLUP_TOTAL_5000', 'TOTAL_REPS', '🦾', 2, 5000, 8, 23, false, true),
('PULLUP_TOTAL_10000', 'TOTAL_REPS', '🦾', 2, 10000, 10, 24, false, true),
('PULLUP_TOTAL_50000', 'TOTAL_REPS', '🦾', 2, 50000, 20, 25, false, true),
('PULLUP_TOTAL_100000', 'TOTAL_REPS', '🦾', 2, 100000, 50, 26, true, true),

-- TOTAL_REPS: Squats (7 tiers)
('SQUAT_TOTAL_100', 'TOTAL_REPS', '🦵', 3, 100, 1, 30, false, true),
('SQUAT_TOTAL_500', 'TOTAL_REPS', '🦵', 3, 500, 2, 31, false, true),
('SQUAT_TOTAL_1000', 'TOTAL_REPS', '🦵', 3, 1000, 3, 32, false, true),
('SQUAT_TOTAL_5000', 'TOTAL_REPS', '🦵', 3, 5000, 4, 33, false, true),
('SQUAT_TOTAL_10000', 'TOTAL_REPS', '🦵', 3, 10000, 5, 34, false, true),
('SQUAT_TOTAL_50000', 'TOTAL_REPS', '🦵', 3, 50000, 10, 35, false, true),
('SQUAT_TOTAL_100000', 'TOTAL_REPS', '🦵', 3, 100000, 25, 36, true, true),

-- TOTAL_REPS: Abs (7 tiers)
('ABS_TOTAL_100', 'TOTAL_REPS', '🛡️', 4, 100, 1, 40, false, true),
('ABS_TOTAL_500', 'TOTAL_REPS', '🛡️', 4, 500, 2, 41, false, true),
('ABS_TOTAL_1000', 'TOTAL_REPS', '🛡️', 4, 1000, 3, 42, false, true),
('ABS_TOTAL_5000', 'TOTAL_REPS', '🛡️', 4, 5000, 4, 43, false, true),
('ABS_TOTAL_10000', 'TOTAL_REPS', '🛡️', 4, 10000, 5, 44, false, true),
('ABS_TOTAL_50000', 'TOTAL_REPS', '🛡️', 4, 50000, 10, 45, false, true),
('ABS_TOTAL_100000', 'TOTAL_REPS', '🛡️', 4, 100000, 25, 46, true, true),

-- MAX_REPS: Push-ups (5 tiers)
('PUSHUP_MAX_20', 'MAX_REPS', '⚡', 1, 20, 2, 100, false, true),
('PUSHUP_MAX_50', 'MAX_REPS', '⚡', 1, 50, 5, 101, false, true),
('PUSHUP_MAX_100', 'MAX_REPS', '⚡', 1, 100, 10, 102, false, true),
('PUSHUP_MAX_200', 'MAX_REPS', '⚡', 1, 200, 20, 103, false, true),
('PUSHUP_MAX_500', 'MAX_REPS', '⚡', 1, 500, 50, 104, true, true),

-- MAX_REPS: Pull-ups (5 tiers)
('PULLUP_MAX_1', 'MAX_REPS', '⚡', 2, 1, 1, 110, false, true),
('PULLUP_MAX_5', 'MAX_REPS', '⚡', 2, 5, 5, 111, false, true),
('PULLUP_MAX_10', 'MAX_REPS', '⚡', 2, 10, 10, 112, false, true),
('PULLUP_MAX_20', 'MAX_REPS', '⚡', 2, 20, 20, 113, false, true),
('PULLUP_MAX_50', 'MAX_REPS', '⚡', 2, 50, 50, 114, true, true),

-- MAX_REPS: Squats (5 tiers)
('SQUAT_MAX_20', 'MAX_REPS', '⚡', 3, 20, 2, 120, false, true),
('SQUAT_MAX_50', 'MAX_REPS', '⚡', 3, 50, 5, 121, false, true),
('SQUAT_MAX_100', 'MAX_REPS', '⚡', 3, 100, 10, 122, false, true),
('SQUAT_MAX_200', 'MAX_REPS', '⚡', 3, 200, 20, 123, false, true),
('SQUAT_MAX_500', 'MAX_REPS', '⚡', 3, 500, 50, 124, true, true),

-- MAX_REPS: Abs (5 tiers)
('ABS_MAX_20', 'MAX_REPS', '⚡', 4, 20, 2, 130, false, true),
('ABS_MAX_30', 'MAX_REPS', '⚡', 4, 30, 3, 131, false, true),
('ABS_MAX_100', 'MAX_REPS', '⚡', 4, 100, 10, 132, false, true),
('ABS_MAX_200', 'MAX_REPS', '⚡', 4, 200, 20, 133, false, true),
('ABS_MAX_500', 'MAX_REPS', '⚡', 4, 500, 50, 134, true, true),

-- WORKOUT_COUNT: Total workouts (7 tiers)
('WORKOUT_FIRST', 'WORKOUT_COUNT', '⚔️', NULL, 1, 1, 300, false, true),
('WORKOUT_10', 'WORKOUT_COUNT', '⚔️', NULL, 10, 2, 301, false, true),
('WORKOUT_50', 'WORKOUT_COUNT', '⚔️', NULL, 50, 5, 302, false, true),
('WORKOUT_100', 'WORKOUT_COUNT', '⚔️', NULL, 100, 10, 303, false, true),
('WORKOUT_250', 'WORKOUT_COUNT', '⚔️', NULL, 250, 20, 304, false, true),
('WORKOUT_500', 'WORKOUT_COUNT', '⚔️', NULL, 500, 25, 305, false, true),
('WORKOUT_1000', 'WORKOUT_COUNT', '⚔️', NULL, 1000, 100, 307, true, true),

-- SOCIAL: Following and followers (6 tiers)
('SOCIAL_FOLLOWING_1', 'SOCIAL', '🤝', NULL, 1, 2, 400, false, true),
('SOCIAL_FOLLOWING_5', 'SOCIAL', '🤝', NULL, 5, 5, 401, false, true),
('SOCIAL_FOLLOWER_1', 'SOCIAL', '🤝', NULL, 1, 5, 402, false, true),
('SOCIAL_FOLLOWER_5', 'SOCIAL', '🤝', NULL, 5, 10, 403, false, true),
('SOCIAL_FOLLOWER_10', 'SOCIAL', '🤝', NULL, 10, 20, 404, false, true),
('SOCIAL_FOLLOWER_20', 'SOCIAL', '🤝', NULL, 20, 50, 405, false, true),

-- LEADERBOARD: Ranking achievements (7 tiers)
('LEADERBOARD_TOP100', 'LEADERBOARD', '🏅', NULL, 100, 1, 500, false, true),
('LEADERBOARD_TOP10', 'LEADERBOARD', '🏅', NULL, 10, 2, 501, false, true),
('LEADERBOARD_TOP5', 'LEADERBOARD', '🏅', NULL, 5, 3, 502, false, true),
('LEADERBOARD_BRONZE', 'LEADERBOARD', '🏅', NULL, 3, 5, 503, false, true),
('LEADERBOARD_SILVER', 'LEADERBOARD', '🏅', NULL, 2, 8, 504, false, true),
('LEADERBOARD_GOLD', 'LEADERBOARD', '🏅', NULL, 1, 10, 505, false, true),
('LEADERBOARD_DOMINATOR', 'LEADERBOARD', '🏅', NULL, 4, 10, 506, true, true),

-- REFERRAL: Referral achievements (7 tiers)
('REFERRAL_1', 'REFERRAL', '🤝', NULL, 1, 5, 600, false, true),
('REFERRAL_3', 'REFERRAL', '🤝', NULL, 3, 10, 601, false, true),
('REFERRAL_5', 'REFERRAL', '🤝', NULL, 5, 15, 602, false, true),
('REFERRAL_10', 'REFERRAL', '🤝', NULL, 10, 20, 603, false, true),
('REFERRAL_30', 'REFERRAL', '🤝', NULL, 30, 30, 604, false, true),
('REFERRAL_100', 'REFERRAL', '🤝', NULL, 100, 100, 605, false, true),
('REFERRAL_250', 'REFERRAL', '🤝', NULL, 250, 250, 606, false, true),

-- STREAK: Consecutive days (5 tiers)
('STREAK_5', 'STREAK', '🌾', NULL, 5, 3, 699, false, true),
('STREAK_10', 'STREAK', '🌱', NULL, 10, 5, 700, false, true),
('STREAK_20', 'STREAK', '🌿', NULL, 20, 10, 701, false, true),
('STREAK_50', 'STREAK', '🌳', NULL, 50, 25, 702, false, true),
('STREAK_100', 'STREAK', '🌍', NULL, 100, 50, 703, true, true);

--changeset unified-achievements:4
-- Вставка русских локализаций для всех 80 достижений
INSERT INTO achievements (achievement_definition_id, language, title, description, created_at, updated_at)
SELECT ad.id, 'ru', 'Малой', 'Выполни 100 отжиманий всего. Первые шаги юного воина.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_100' UNION ALL
SELECT ad.id, 'ru', 'Ученик', 'Выполни 500 отжиманий всего. Воин княжеской дружины.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_500' UNION ALL
SELECT ad.id, 'ru', 'Оруженосец', 'Выполни 1 000 отжиманий всего. Отважный воин Руси.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_1000' UNION ALL
SELECT ad.id, 'ru', 'Дружинник', 'Выполни 5 000 отжиманий всего. Легендарный силач былин.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_5000' UNION ALL
SELECT ad.id, 'ru', 'Витязь', 'Выполни 10 000 отжиманий всего. Усилен технологиями.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_10000' UNION ALL
SELECT ad.id, 'ru', 'Богатырь', 'Выполни 50 000 отжиманий всего. Человек-машина силы.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_50000' UNION ALL
SELECT ad.id, 'ru', 'Кибер-воин', 'Выполни 100 000 отжиманий всего. Превзошел физику.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_TOTAL_100000' UNION ALL
SELECT ad.id, 'ru', 'Медвежонок', 'Выполни 100 подтягиваний всего. Детеныш учится хватать.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_100' UNION ALL
SELECT ad.id, 'ru', 'Медведь', 'Выполни 500 подтягиваний всего. Ловкий и сильный.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_500' UNION ALL
SELECT ad.id, 'ru', 'Медведь-охотник', 'Выполни 1 000 подтягиваний всего. Сила дикого зверя.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_1000' UNION ALL
SELECT ad.id, 'ru', 'Медведь-воин', 'Выполни 5 000 подтягиваний всего. Властелин тайги.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_5000' UNION ALL
SELECT ad.id, 'ru', 'Лютый медведь', 'Выполни 10 000 подтягиваний всего. Железные когти.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_10000' UNION ALL
SELECT ad.id, 'ru', 'Хозяин леса', 'Выполни 50 000 подтягиваний всего. Достиг облаков.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_50000' UNION ALL
SELECT ad.id, 'ru', 'Кибер-медведь', 'Выполни 100 000 подтягиваний всего. Космическая мощь.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_TOTAL_100000' UNION ALL
SELECT ad.id, 'ru', 'Жеребенок', 'Выполни 100 приседаний всего. Зарождение силы.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_100' UNION ALL
SELECT ad.id, 'ru', 'Молодой конь', 'Выполни 500 приседаний всего. Крепнущие корни.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_500' UNION ALL
SELECT ad.id, 'ru', 'Конь', 'Выполни 1 000 приседаний всего. Несгибаемый вечный.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_1000' UNION ALL
SELECT ad.id, 'ru', 'Жеребец', 'Выполни 5 000 приседаний всего. Твердость камня.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_5000' UNION ALL
SELECT ad.id, 'ru', 'Вороной', 'Выполни 10 000 приседаний всего. Металлическая основа.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_10000' UNION ALL
SELECT ad.id, 'ru', 'Боевой конь', 'Выполни 50 000 приседаний всего. Фундамент миров.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_50000' UNION ALL
SELECT ad.id, 'ru', 'Пегас', 'Выполни 100 000 приседаний всего. Держишь планеты.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_TOTAL_100000' UNION ALL
SELECT ad.id, 'ru', 'Тряпка', 'Выполни 100 повторений всего. Первая защита тела.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_100' UNION ALL
SELECT ad.id, 'ru', 'Разломанный щит', 'Выполни 500 повторений всего. Доспехи знатного воина.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_500' UNION ALL
SELECT ad.id, 'ru', 'Тренировочный щит', 'Выполни 1 000 повторений всего. Защита храброго.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_1000' UNION ALL
SELECT ad.id, 'ru', 'Деревянный щит', 'Выполни 5 000 повторений всего. Непробиваемая защита.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_5000' UNION ALL
SELECT ad.id, 'ru', 'Окованный щит', 'Выполни 10 000 повторений всего. Технологическая защита.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_10000' UNION ALL
SELECT ad.id, 'ru', 'Железный щит', 'Выполни 50 000 повторений всего. Металлическая стена.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_50000' UNION ALL
SELECT ad.id, 'ru', 'Щит героя', 'Выполни 100 000 повторений всего. Энергетический щит.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_TOTAL_100000' UNION ALL
SELECT ad.id, 'ru', 'Палка', 'Сделай 20 отжиманий за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_MAX_20' UNION ALL
SELECT ad.id, 'ru', 'Дубина', 'Сделай 50 отжиманий за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_MAX_50' UNION ALL
SELECT ad.id, 'ru', 'Ржавый меч', 'Сделай 100 отжиманий за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_MAX_100' UNION ALL
SELECT ad.id, 'ru', 'Меч воина', 'Сделай 200 отжиманий за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_MAX_200' UNION ALL
SELECT ad.id, 'ru', 'Меч-кладенец', 'Сделай 500 отжиманий за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PUSHUP_MAX_500' UNION ALL
SELECT ad.id, 'ru', 'Залез на дерево', 'Сделай 1 подтягивание. Начало восхождения.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_MAX_1' UNION ALL
SELECT ad.id, 'ru', 'Забрался на холм', 'Сделай 5 подтягиваний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_MAX_5' UNION ALL
SELECT ad.id, 'ru', 'Покорил скалу', 'Сделай 10 подтягиваний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_MAX_10' UNION ALL
SELECT ad.id, 'ru', 'Взошел на гору', 'Сделай 20 подтягиваний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_MAX_20' UNION ALL
SELECT ad.id, 'ru', 'Достиг облаков', 'Сделай 50 подтягиваний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'PULLUP_MAX_50' UNION ALL
SELECT ad.id, 'ru', 'Рваные портки', 'Сделай 20 приседаний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_MAX_20' UNION ALL
SELECT ad.id, 'ru', 'Льняные портки', 'Сделай 50 приседаний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_MAX_50' UNION ALL
SELECT ad.id, 'ru', 'Штаны', 'Сделай 100 приседаний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_MAX_100' UNION ALL
SELECT ad.id, 'ru', 'Кольчужные поножи', 'Сделай 200 приседаний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_MAX_200' UNION ALL
SELECT ad.id, 'ru', 'Железные ноги', 'Сделай 500 приседаний за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SQUAT_MAX_500' UNION ALL
SELECT ad.id, 'ru', 'Рваная рубаха', 'Сделай 20 повторений за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_MAX_20' UNION ALL
SELECT ad.id, 'ru', 'Льняная рубаха', 'Сделай 30 повторений за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_MAX_30' UNION ALL
SELECT ad.id, 'ru', 'Стеганка', 'Сделай 100 повторений за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_MAX_100' UNION ALL
SELECT ad.id, 'ru', 'Кольчуга', 'Сделай 200 повторений за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_MAX_200' UNION ALL
SELECT ad.id, 'ru', 'Рыцарский доспех', 'Сделай 500 повторений за один раз.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'ABS_MAX_500' UNION ALL
SELECT ad.id, 'ru', 'Первый удар молота', 'Выполни 1 тренировку. Путь дисциплины начинается с первого шага.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_FIRST' UNION ALL
SELECT ad.id, 'ru', 'Ученик кузни', 'Выполни 10 тренировок. Дисциплина крепнет, закалка начинается.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_10' UNION ALL
SELECT ad.id, 'ru', 'Подмастерье', 'Выполни 50 тренировок. Воля закаляется в огне упорства.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_50' UNION ALL
SELECT ad.id, 'ru', 'Кузнец своего тела', 'Выполни 100 тренировок. Железная дисциплина куёт силу.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_100' UNION ALL
SELECT ad.id, 'ru', 'Мастер-оружейник', 'Выполни 250 тренировок. Дисциплина стала второй натурой.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_250' UNION ALL
SELECT ad.id, 'ru', 'Небесный Кузнец ', 'Выполни 500 тренировок. Воля несгибаема, тело — булат.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_500' UNION ALL
SELECT ad.id, 'ru', 'Творец бессмертия', 'Выполни 1000 тренировок. Дисциплина достигла божественного уровня.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'WORKOUT_1000' UNION ALL
SELECT ad.id, 'ru', 'Первый товарищ', 'Подпишись на 1 пользователя. Путь вместе веселей.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWING_1' UNION ALL
SELECT ad.id, 'ru', 'Дружина собрана', 'Подпишись на 5 пользователей. Княжеская свита готова.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWING_5' UNION ALL
SELECT ad.id, 'ru', 'Я тебя где-то видел', 'Получи 1 подписчика. Тебя замечают в народе.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWER_1' UNION ALL
SELECT ad.id, 'ru', 'Уважаемый воин', 'Получи 5 подписчиков. Тебя замечают в народе.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWER_5' UNION ALL
SELECT ad.id, 'ru', 'Атаман ватаги', 'Получи 10 подписчиков. Ведешь за собой других.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWER_10' UNION ALL
SELECT ad.id, 'ru', 'Князь земли', 'Получи 20 подписчиков. Властитель и вдохновитель.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'SOCIAL_FOLLOWER_20' UNION ALL
SELECT ad.id, 'ru', 'Первый бой', 'Попади в лидерборд. Имя твоё записано.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_TOP100' UNION ALL
SELECT ad.id, 'ru', 'Опытный боец', 'Топ-10 по любому упражнению. Среди лучших.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_TOP10' UNION ALL
SELECT ad.id, 'ru', 'Ветеран', 'Топ-5 по упражнению. Ближний круг власти.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_TOP5' UNION ALL
SELECT ad.id, 'ru', 'Бронзовый щит', '3 место в лидерборде. На подиуме почёта.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_BRONZE' UNION ALL
SELECT ad.id, 'ru', 'Серебряный меч', '2 место в лидерборде. Почти на вершине.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_SILVER' UNION ALL
SELECT ad.id, 'ru', 'Золотая корона', '1 место в лидерборде. Ты — царь горы!', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_GOLD' UNION ALL
SELECT ad.id, 'ru', 'Доминатор', 'Первый в 4 упражнениях. Абсолютное доминирование.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'LEADERBOARD_DOMINATOR' UNION ALL
SELECT ad.id, 'ru', 'Один в поле не воин', 'Пригласи 1 друга. Один в поле не воин.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_1' UNION ALL
SELECT ad.id, 'ru', 'Отряд бойцов', 'Пригласи 3 друзей. Отряд бойцов готов к действию.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_3' UNION ALL
SELECT ad.id, 'ru', 'Малая дружина', 'Пригласи 5 друзей. Малая дружина собрана.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_5' UNION ALL
SELECT ad.id, 'ru', 'Командир десятки', 'Пригласи 10 друзей. Командуешь десятком воинов.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_10' UNION ALL
SELECT ad.id, 'ru', 'Командир взвода', 'Пригласи 30 друзей. Командир взвода ведёт бойцов.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_30' UNION ALL
SELECT ad.id, 'ru', 'Сотник', 'Пригласи 100 друзей. Сотник объединил воинов под знаменем.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_100' UNION ALL
SELECT ad.id, 'ru', 'Воевода', 'Пригласи 250 друзей. Воевода ведёт непобедимую армию.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'REFERRAL_250' UNION ALL
SELECT ad.id, 'ru', 'Зерно', 'Тренируйся 5 дней подряд. Зерно проросло.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'STREAK_5' UNION ALL
SELECT ad.id, 'ru', 'Росток дуба', 'Тренируйся 10 дней подряд. Посажено семя великой силы.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'STREAK_10' UNION ALL
SELECT ad.id, 'ru', 'Молодой дуб', 'Тренируйся 20 дней подряд. Росток пробивается к свету.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'STREAK_20' UNION ALL
SELECT ad.id, 'ru', 'Столетний дуб', 'Тренируйся 50 дней подряд. Несгибаемый дуб силы.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'STREAK_50' UNION ALL
SELECT ad.id, 'ru', 'Мировое древо', 'Тренируйся 100 дней подряд. Ирий-дерево связывает небо и землю.', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM achievement_definitions ad WHERE ad.code = 'STREAK_100';
