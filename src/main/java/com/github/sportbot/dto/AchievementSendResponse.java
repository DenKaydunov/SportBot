package com.github.sportbot.dto;

import java.time.LocalDateTime;

public record AchievementSendResponse(
        String message,
        int totalUsersMessaged,
        int failedMessages,
        LocalDateTime sentAt
) {
    public static AchievementSendResponse success(int totalUsers, int failed) {
        return new AchievementSendResponse(
                "Achievement congratulations sent successfully",
                totalUsers,
                failed,
                LocalDateTime.now()
        );
    }

    public static AchievementSendResponse noContent() {
        return new AchievementSendResponse(
                "No achievements to send - message was empty",
                0,
                0,
                LocalDateTime.now()
        );
    }
}
