--liquibase formatted sql

--changeset Alexsandr Mojarovskiy:7
ALTER TABLE users
    ADD COLUMN age INTEGER,
    ADD COLUMN sex VARCHAR(10),
    ADD COLUMN language VARCHAR(10);