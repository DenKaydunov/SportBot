--liquibase formatted sql

--changeset deniskaydunov:26
-- Drop deprecated achievement tables after successful migration to unified achievement system
-- These tables were deprecated in changeset 20 and are no longer used

DROP TABLE IF EXISTS achievements_deprecated;
DROP TABLE IF EXISTS streak_milestone_deprecated;
DROP TABLE IF EXISTS referral_milestone_deprecated;
