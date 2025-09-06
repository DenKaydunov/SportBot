package com.github.sportbot.exception;

public class ProgramNotFoundException extends RuntimeException {

    private static final String PROGRAM_NOT_FOUND_MESSAGE = "Program not found";

    public ProgramNotFoundException() {
        super(PROGRAM_NOT_FOUND_MESSAGE);
    }
}
