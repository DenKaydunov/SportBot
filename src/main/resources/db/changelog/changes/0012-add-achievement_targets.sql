--liquibase formatted sql

--changeset edikrusinov:1
CREATE TABLE achievement_targets (
    id SERIAL PRIMARY KEY,
    value INTEGER NOT NULL UNIQUE,
    description VARCHAR(255)
);

INSERT INTO achievement_targets(value) VALUES
    (500),
    (1000),
    (5000),
    (10000),
    (20000),
    (50000);