--liquibase formatted sql

--changeset deniskaydunov:17 validCheckSum:9:47aad41889c5f36e8494cbd16825f17b
CREATE TABLE referral_milestone
(
    id                  BIGSERIAL PRIMARY KEY,
    referrals_required  INTEGER      NOT NULL,
    reward_ton          INTEGER      NOT NULL DEFAULT 0,
    title               VARCHAR(255) NOT NULL,
    description         TEXT
);

ALTER TABLE achievements ADD COLUMN IF NOT EXISTS referral_milestone_id BIGINT;
ALTER TABLE achievements ADD CONSTRAINT fk_achievements_referral_milestone
    FOREIGN KEY (referral_milestone_id)
        REFERENCES referral_milestone (id)
        ON DELETE CASCADE;

INSERT INTO referral_milestone (referrals_required, reward_ton, title, description)
VALUES
    (1, 0, 'Called a Comrade', 'Invited your first friend'),
    (3, 0, 'Squad Forming', 'Your squad is taking shape'),
    (10, 0, 'Commander of Ten', 'Leading a squad of 10'),
    (25, 0, 'Centurion', 'Command of 25 warriors'),
    (50, 0, 'Voivode', 'Leading 50 strong'),
    (100, 0, 'Prince-Gatherer', 'United 100 warriors'),
    (250, 0, 'Tsar-Unifier', 'United 250 under your banner'),
    (500, 0, 'Legion Creator', 'Built a legendary force of 500');
