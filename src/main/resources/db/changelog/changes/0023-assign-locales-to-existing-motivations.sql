--liquibase formatted sql

--changeset deniskaydunov:24

-- Assign 'en' locale to English push-up messages (lines 5-14 in test data)
UPDATE motivation
SET locale = 'en'
WHERE message IN (
    'No pain, no gain!',
    'Push harder than yesterday if you want a different tomorrow.',
    'Discipline beats motivation.',
    'Small progress is still progress.',
    'Your only limit is you.',
    'Fall down seven times, stand up eight.',
    'Excuses don''t burn calories.',
    'Consistency is the key to results.',
    'Sweat is fat crying.',
    'Stronger every day.'
);

-- All remaining messages keep default 'ru' locale (already set by default in previous changeset)
-- Russian push-ups: lines 16-113
-- Russian pull-ups: lines 115-125
-- Russian squats: lines 127-170
