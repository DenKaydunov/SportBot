package com.github.sportbot.exception;

public class UserAlreadyExistsException extends RuntimeException {

    private static final String USER_ALREADY_EXISTS_MESSAGE = "User already exists";

    public UserAlreadyExistsException() {
        super(USER_ALREADY_EXISTS_MESSAGE);
    }
}
