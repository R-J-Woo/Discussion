package com.discussion.ryu.exception;

public class PasswordConfirmationMismatchException extends RuntimeException {

    public PasswordConfirmationMismatchException(String message) {
        super(message);
    }
}
