--liquibase formatted sql

--changeset Alexsandr Mojarovskiy:7
ALTER TABLE users ADD COLUMN IF NOT EXISTS age INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sex VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS language VARCHAR(10);