package com.github.sportbot.event;

import com.github.sportbot.model.ExerciseType;
import com.github.sportbot.model.User;
import com.github.sportbot.repository.ExerciseTypeRepository;
import com.github.sportbot.repository.UserRepository;
import com.github.sportbot.service.NotificationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration test to verify that WorkoutEventListener only processes events
 * after successful transaction commit, and skips events when transaction is rolled back.
 */
@SpringBootTest
@ActiveProfiles("test")
class WorkoutEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @SpyBean
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExerciseTypeRepository exerciseTypeRepository;

    private User testUser;
    private ExerciseType testExerciseType;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .telegramId(999999L)
                .fullName("Test User")
                .language("ru")
                .build();
        testUser = userRepository.save(testUser);

        testExerciseType = exerciseTypeRepository.findByCode("push_up")
                .orElseThrow(() -> new IllegalStateException("push_up exercise type not found"));
    }

    @AfterEach
    void tearDown() {
        if (testUser != null && testUser.getId() != null) {
            userRepository.deleteById(testUser.getId());
        }
    }

    @Test
    void workoutRecordedEvent_AfterTransactionCommit_TriggersNotification() {
        // Given
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        // When: Publish event inside a transaction that commits successfully
        txTemplate.execute(status -> {
            eventPublisher.publishEvent(new WorkoutRecordedEvent(testUser, testExerciseType, 50));
            return null;
        });

        // Then: NotificationService should be called after transaction commits
        verify(notificationService, timeout(1000).times(1))
                .notifyFollowersAboutWorkout(eq(testUser), eq(testExerciseType), eq(50));
    }

    @Test
    void workoutRecordedEvent_AfterTransactionRollback_DoesNotTriggerNotification() {
        // Given
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);
        reset(notificationService); // Clear any previous invocations

        // When: Publish event inside a transaction that rolls back
        try {
            txTemplate.execute(status -> {
                eventPublisher.publishEvent(new WorkoutRecordedEvent(testUser, testExerciseType, 50));
                // Force rollback
                status.setRollbackOnly();
                return null;
            });
        } catch (Exception e) {
            // Expected - transaction rolled back
        }

        // Then: NotificationService should NOT be called
        // Wait a bit to ensure the event listener had a chance to run (it shouldn't)
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        verify(notificationService, never())
                .notifyFollowersAboutWorkout(any(), any(), anyInt());
    }

    @Test
    void achievementUnlockedEvent_AfterTransactionCommit_EventHandled() {
        // Given
        TransactionTemplate txTemplate = new TransactionTemplate(transactionManager);

        // When: Publish achievement event inside a transaction that commits
        txTemplate.execute(status -> {
            eventPublisher.publishEvent(new AchievementUnlockedEvent(testUser, java.util.List.of()));
            return null;
        });

        // Then: Event should be processed without errors
        // (Currently the listener just logs, no external calls to verify)
        // This test mainly ensures the listener is properly configured
    }
}
