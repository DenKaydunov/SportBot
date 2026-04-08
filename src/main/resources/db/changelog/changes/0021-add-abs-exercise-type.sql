--liquibase formatted sql

--changeset deniskaydunov:21
INSERT INTO exercise_types (code, title)
SELECT 'abs', 'Пресс'
WHERE NOT EXISTS (
    SELECT 1 FROM exercise_types WHERE code = 'abs'
);
