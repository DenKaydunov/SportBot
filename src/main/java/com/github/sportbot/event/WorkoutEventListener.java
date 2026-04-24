package com.github.sportbot.event;

import com.github.sportbot.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener that handles workout-related events after transaction commit.
 * This ensures notifications are only sent after data is successfully persisted.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkoutEventListener {

    private final NotificationService notificationService;

    /**
     * Handle workout recorded event after transaction commits successfully.
     * Sends notifications to followers about the workout.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWorkoutRecorded(WorkoutRecordedEvent event) {
        log.debug("Handling workout recorded event for user {} after transaction commit",
                event.getUser().getTelegramId());

        try {
            notificationService.notifyFollowersAboutWorkout(
                event.getUser(),
                event.getExerciseType(),
                event.getCount()
            );
        } catch (Exception e) {
            log.error("Failed to send workout notifications for user {}",
                    event.getUser().getTelegramId(), e);
            // Don't rethrow - notification failure shouldn't affect the workout recording
        }
    }

    /**
     * Handle achievement unlocked event after transaction commits successfully.
     * This is a placeholder for future achievement notification logic.
     * Currently, achievement messages are returned synchronously in the API response.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAchievementUnlocked(AchievementUnlockedEvent event) {
        log.debug("User {} unlocked {} achievements after transaction commit",
                event.getUser().getTelegramId(),
                event.getUnlockedAchievements().size());

        // Future: Send push notifications or separate messages for achievements
        // Currently, achievements are displayed in the workout response message
    }
}
