package com.github.sportbot.exception;

public class UserNotFoundException extends RuntimeException {

    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    public UserNotFoundException() {
        super(USER_NOT_FOUND_MESSAGE);
    }
}
