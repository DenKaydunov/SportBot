--liquibase formatted sql

--changeset deniskaydunov:22
ALTER TABLE motivation ADD COLUMN locale VARCHAR(10) NOT NULL DEFAULT 'ru';

--changeset deniskaydunov:23
CREATE INDEX idx_motivation_exercise_locale ON motivation(exercise_type_id, locale);
