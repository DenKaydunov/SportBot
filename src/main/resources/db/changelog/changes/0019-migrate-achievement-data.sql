--liquibase formatted sql

--changeset deniskaydunov:19
-- Migrate streak_milestone to achievement_definitions
INSERT INTO achievement_definitions (code, category, emoji, title_key, description_key, target_value, reward_ton, sort_order)
SELECT
    CONCAT('STREAK_', days_required, '_DAYS'),
    'STREAK',
    CASE
        WHEN days_required = 10 THEN '🔥'
        WHEN days_required = 20 THEN '💪'
        WHEN days_required = 50 THEN '👑'
        ELSE '⭐'
    END,
    CONCAT('achievement.streak.', days_required, '.title'),
    CONCAT('achievement.streak.', days_required, '.description'),
    days_required,
    reward_ton,
    days_required
FROM streak_milestone
ORDER BY days_required;

-- Migrate referral_milestone to achievement_definitions
INSERT INTO achievement_definitions (code, category, emoji, title_key, description_key, target_value, reward_ton, sort_order)
SELECT
    CONCAT('REFERRAL_', referrals_required),
    'REFERRAL',
    CASE
        WHEN referrals_required = 1 THEN '🤝'
        WHEN referrals_required = 3 THEN '👥'
        WHEN referrals_required = 10 THEN '👨‍👨‍👦'
        WHEN referrals_required = 25 THEN '👑'
        WHEN referrals_required = 50 THEN '🏆'
        WHEN referrals_required = 100 THEN '🌟'
        WHEN referrals_required = 250 THEN '💎'
        WHEN referrals_required = 500 THEN '🚀'
        ELSE '⭐'
    END,
    CONCAT('achievement.referral.', referrals_required, '.title'),
    CONCAT('achievement.referral.', referrals_required, '.description'),
    referrals_required,
    reward_ton,
    referrals_required
FROM referral_milestone
ORDER BY referrals_required;

-- Migrate streak achievements from achievements table to user_achievements
INSERT INTO user_achievements (user_id, achievement_definition_id, current_progress, achieved_date, notified)
SELECT
    a.user_id,
    ad.id,
    ad.target_value,
    a.achieved_date,
    true
FROM achievements a
JOIN streak_milestone sm ON a.milestone_id = sm.id
JOIN achievement_definitions ad ON ad.code = CONCAT('STREAK_', sm.days_required, '_DAYS')
WHERE a.milestone_id IS NOT NULL;

-- Migrate referral achievements from achievements table to user_achievements
INSERT INTO user_achievements (user_id, achievement_definition_id, current_progress, achieved_date, notified)
SELECT
    a.user_id,
    ad.id,
    ad.target_value,
    a.achieved_date,
    true
FROM achievements a
JOIN referral_milestone rm ON a.referral_milestone_id = rm.id
JOIN achievement_definitions ad ON ad.code = CONCAT('REFERRAL_', rm.referrals_required)
WHERE a.referral_milestone_id IS NOT NULL;
