--liquibase formatted sql

--changeset alexmazharouski:1
CREATE TABLE users (
                       id                   SERIAL PRIMARY KEY,
                       full_name            VARCHAR(255) NOT NULL,
                       telegram_id          BIGINT NOT NULL UNIQUE,
                       referrer_telegram_id BIGINT,
                       send_pulse_id        VARCHAR(255),
                       is_subscribed        BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                       updated_at           TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                       remind_time          TIME
);

CREATE TABLE user_events (
                             id          BIGSERIAL PRIMARY KEY,
                             user_id     INTEGER NOT NULL,
                             event_type  VARCHAR(100) NOT NULL,
                             payload     VARCHAR,
                             timestamp   TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
                             CONSTRAINT fk_user_events_user
                                 FOREIGN KEY (user_id)
                                     REFERENCES users(id)
                                     ON DELETE CASCADE
);

CREATE TABLE exercise_types (
                                id     SERIAL PRIMARY KEY,
                                code   VARCHAR(50) NOT NULL UNIQUE,
                                title  VARCHAR(255) NOT NULL
);

CREATE TABLE user_programs (
                               user_id          INTEGER NOT NULL,
                               exercise_type_id INTEGER NOT NULL,
                               current_max      INTEGER NOT NULL,
                               day_number       INTEGER NOT NULL,
                               PRIMARY KEY (user_id, exercise_type_id),
                               CONSTRAINT fk_user_programs_user
                                   FOREIGN KEY (user_id)
                                       REFERENCES users(id)
                                       ON DELETE CASCADE,
                               CONSTRAINT fk_user_programs_exercise
                                   FOREIGN KEY (exercise_type_id)
                                       REFERENCES exercise_types(id)
                                       ON DELETE CASCADE
);

CREATE TABLE exercise_record (
                                 id               BIGSERIAL PRIMARY KEY,
                                 user_id          INTEGER NOT NULL,
                                 exercise_type_id INTEGER NOT NULL,
                                 count            INTEGER NOT NULL,
                                 date             DATE    NOT NULL,
                                 CONSTRAINT fk_wh_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE,
                                 CONSTRAINT fk_wh_exercise
                                     FOREIGN KEY (exercise_type_id)
                                         REFERENCES exercise_types(id)
                                         ON DELETE CASCADE
);

CREATE TABLE user_max_history (
                                  id               BIGSERIAL PRIMARY KEY,
                                  user_id          INTEGER NOT NULL,
                                  exercise_type_id INTEGER NOT NULL,
                                  max_value        INTEGER NOT NULL,
                                  date             TIMESTAMP    NOT NULL,
                                  CONSTRAINT fk_umh_user
                                      FOREIGN KEY (user_id)
                                          REFERENCES users(id)
                                          ON DELETE CASCADE,
                                  CONSTRAINT fk_umh_exercise
                                      FOREIGN KEY (exercise_type_id)
                                          REFERENCES exercise_types(id)
                                          ON DELETE CASCADE
);

CREATE TABLE motivation (
                            id               SERIAL PRIMARY KEY,
                            exercise_type_id INTEGER NOT NULL,
                            message          TEXT    NOT NULL,
                            CONSTRAINT fk_motivation_exercise
                                FOREIGN KEY (exercise_type_id)
                                    REFERENCES exercise_types(id)
                                    ON DELETE CASCADE
);
