--liquibase formatted sql

--changeset deniskaydunov:18
CREATE TABLE achievement_definitions
(
    id                BIGSERIAL PRIMARY KEY,
    code              VARCHAR(100) UNIQUE NOT NULL,
    category          VARCHAR(50)         NOT NULL,
    emoji             VARCHAR(10)         NOT NULL,
    title_key         VARCHAR(200)        NOT NULL,
    description_key   VARCHAR(200)        NOT NULL,

    target_value      INTEGER             NOT NULL,
    exercise_type_id  BIGINT,
    reward_ton        INTEGER             NOT NULL DEFAULT 0,

    sort_order        INTEGER             NOT NULL DEFAULT 0,
    is_legendary      BOOLEAN             NOT NULL DEFAULT false,
    is_active         BOOLEAN             NOT NULL DEFAULT true,

    created_at        TIMESTAMP           DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_achievement_def_exercise_type
        FOREIGN KEY (exercise_type_id)
            REFERENCES exercise_types (id)
            ON DELETE SET NULL
);

CREATE INDEX idx_achievement_definitions_category ON achievement_definitions(category);
CREATE INDEX idx_achievement_definitions_is_active ON achievement_definitions(is_active);
CREATE INDEX idx_achievement_definitions_exercise_type ON achievement_definitions(exercise_type_id);

CREATE TABLE user_achievements
(
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     INTEGER   NOT NULL,
    achievement_definition_id   BIGINT    NOT NULL,

    current_progress            INTEGER   NOT NULL DEFAULT 0,
    achieved_date               DATE,

    notified                    BOOLEAN   NOT NULL DEFAULT false,
    created_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_user_achievements_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_user_achievements_definition
        FOREIGN KEY (achievement_definition_id)
            REFERENCES achievement_definitions (id)
            ON DELETE CASCADE,

    CONSTRAINT unique_user_achievement UNIQUE(user_id, achievement_definition_id)
);

CREATE INDEX idx_user_achievements_user_id ON user_achievements(user_id);
CREATE INDEX idx_user_achievements_achieved_date ON user_achievements(achieved_date);
CREATE INDEX idx_user_achievements_notified ON user_achievements(notified);
