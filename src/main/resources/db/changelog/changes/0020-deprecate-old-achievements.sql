--liquibase formatted sql

--changeset deniskaydunov:20
-- Rename old tables for rollback safety (don't delete immediately)
-- After 1-2 releases, if everything works correctly, these can be dropped

ALTER TABLE achievements RENAME TO achievements_deprecated;
ALTER TABLE streak_milestone RENAME TO streak_milestone_deprecated;
ALTER TABLE referral_milestone RENAME TO referral_milestone_deprecated;
