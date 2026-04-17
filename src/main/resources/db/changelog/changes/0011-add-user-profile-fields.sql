--liquibase formatted sql

--changeset Alexsandr Mojarovskiy:7 validCheckSum:9:1e6155d073db209b1beab5f624d9a868
ALTER TABLE users ADD COLUMN IF NOT EXISTS age INTEGER;
ALTER TABLE users ADD COLUMN IF NOT EXISTS sex VARCHAR(10);
ALTER TABLE users ADD COLUMN IF NOT EXISTS language VARCHAR(10);