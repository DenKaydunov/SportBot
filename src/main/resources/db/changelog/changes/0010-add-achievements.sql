--liquibase formatted sql

--changeset eduard:9
ALTER TABLE users
    ADD COLUMN IF NOT EXISTS
        balance_ton INTEGER NOT NULL DEFAULT 0;

CREATE TABLE streak_milestone
(
    id            BIGSERIAL PRIMARY KEY,
    days_required INTEGER      NOT NULL,
    reward_ton    INTEGER      NOT NULL default 0,
    title         VARCHAR(255) NOT NULL,
    description   TEXT
);


CREATE TABLE achievements
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       INTEGER NOT NULL,
    milestone_id  BIGINT,
    achieved_date DATE    NOT NULL,
    CONSTRAINT fk_achievements_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

INSERT INTO streak_milestone (days_required, reward_ton, title, description)
VALUES (10, 5, 'Bronze streak', '10 дней подряд без перерыва'),
       (20, 10, 'Silver streak', '20 дней стабильных тренировок'),
       (50, 25, 'Gold streak', '50 дней настоящей силы');