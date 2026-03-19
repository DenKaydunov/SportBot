--liquibase formatted sql

--changeset deniskaydunov:15-fix
-- Удаляем все user_ranks (связи пользователей со старыми рангами)
DELETE FROM user_ranks;

-- Удаляем все старые ранги
DELETE FROM ranks;

-- Делаем exercise_type_id nullable для общих рангов
ALTER TABLE ranks ALTER COLUMN exercise_type_id DROP NOT NULL;
