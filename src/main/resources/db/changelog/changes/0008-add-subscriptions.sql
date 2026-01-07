--liquibase formatted sql

--changeset deniskaydunov:1
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    follower_id INTEGER NOT NULL REFERENCES users(id),
    following_id INTEGER NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE(follower_id, following_id)
);
