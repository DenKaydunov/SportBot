--liquibase formatted sql

--changeset deniskaydunov:1
CREATE TABLE nutrition_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL UNIQUE,
    current_weight DECIMAL(5,2) NOT NULL,
    height DECIMAL(5,2) NOT NULL,
    target_weight DECIMAL(5,2) NOT NULL,
    activity_level VARCHAR(20) NOT NULL,
    dietary_restrictions TEXT,
    weight_change_speed VARCHAR(20) NOT NULL,
    goal_type VARCHAR(20) NOT NULL,
    daily_calorie_target DECIMAL(8,2) NOT NULL,
    protein_target DECIMAL(8,2) NOT NULL,
    carbs_target DECIMAL(8,2) NOT NULL,
    fat_target DECIMAL(8,2) NOT NULL,
    goal_deadline DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_nutrition_profiles_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

--changeset deniskaydunov:2
CREATE TABLE meal_entries (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    food_name VARCHAR(255) NOT NULL,
    calories DECIMAL(8,2) NOT NULL,
    protein DECIMAL(8,2) NOT NULL,
    carbs DECIMAL(8,2) NOT NULL,
    fat DECIMAL(8,2) NOT NULL,
    meal_time TIME,
    date DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_meal_entries_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

--changeset deniskaydunov:3
CREATE TABLE weight_history (
    id BIGSERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL,
    weight DECIMAL(5,2) NOT NULL,
    date DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT fk_weight_history_user
        FOREIGN KEY (user_id)
            REFERENCES users(id)
            ON DELETE CASCADE
);

--changeset deniskaydunov:4
CREATE INDEX idx_meal_entries_user_date ON meal_entries(user_id, date);

--changeset deniskaydunov:5
CREATE INDEX idx_meal_entries_user_created ON meal_entries(user_id, created_at);

--changeset deniskaydunov:6
CREATE INDEX idx_weight_history_user_date ON weight_history(user_id, date);

--changeset deniskaydunov:7
CREATE INDEX idx_nutrition_profiles_user ON nutrition_profiles(user_id);
