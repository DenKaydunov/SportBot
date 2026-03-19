--liquibase formatted sql

--changeset deniskaydunov:15
-- Удаляем user_ranks, связанные со старыми pull_up рангами
DELETE FROM user_ranks
WHERE rank_id IN (SELECT id FROM ranks WHERE exercise_type_id = 2);

-- Удаляем старые 30 рангов для pull_up
DELETE FROM ranks WHERE exercise_type_id = 2;

-- Делаем exercise_type_id nullable для общих рангов
ALTER TABLE ranks ALTER COLUMN exercise_type_id DROP NOT NULL;
