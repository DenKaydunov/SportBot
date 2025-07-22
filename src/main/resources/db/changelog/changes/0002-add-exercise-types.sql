--liquibase formatted sql

--changeset deniskaydunov:1
INSERT INTO exercise_types (code, title)
VALUES
    ('push_up',  'Отжимания'),
    ( 'pull_up',  'Подтягивания'),
    ( 'squat',    'Приседания')
ON CONFLICT (id) DO NOTHING;

