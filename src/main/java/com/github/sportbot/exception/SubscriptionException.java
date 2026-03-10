package com.github.sportbot.exception;

public class SubscriptionException extends RuntimeException {
    private static final String SUBSCRIPTION_MESSAGE = "Cannot subscribe to yourself.";

    public SubscriptionException() {
        super(SUBSCRIPTION_MESSAGE);
    }
}
