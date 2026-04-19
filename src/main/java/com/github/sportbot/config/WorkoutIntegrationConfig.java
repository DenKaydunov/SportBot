package com.github.sportbot.config;

import com.github.sportbot.dto.WorkoutEvent;
import com.github.sportbot.service.WorkoutNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;

import java.time.Duration;

@Configuration
@EnableIntegration
@RequiredArgsConstructor
public class WorkoutIntegrationConfig {

    private final WorkoutNotificationService workoutNotificationService;

    @Bean
    public MessageChannel workoutChannel() {
        return new DirectChannel();
    }

    @Bean
    public IntegrationFlow workoutFlow() {

        return IntegrationFlow.from("workoutChannel")
                .aggregate(a -> a
                        .correlationStrategy(message ->
                                ((WorkoutEvent) message.getPayload()).followerId()
                        )
                        .groupTimeout(Duration.ofHours(1).toMillis())
                        .releaseStrategy(group -> true)
                )
                .handle(workoutNotificationService, "processBatch")
                .get();
    }
}