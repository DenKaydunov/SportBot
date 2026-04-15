package com.github.sportbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "referral")
@Data
public class ReferralProperties {
    /**
     * XP points awarded per invited friend in rating/leaderboard calculations.
     * Each referral adds this amount to the user's total XP.
     * Configurable via application.properties: referral.xp-per-referral=100
     */
    private Integer xpPerReferral = 100;
}
