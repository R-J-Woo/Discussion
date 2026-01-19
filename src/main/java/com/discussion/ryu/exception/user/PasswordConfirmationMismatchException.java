package com.discussion.ryu.exception.user;

public class PasswordConfirmationMismatchException extends RuntimeException {

    public PasswordConfirmationMismatchException(String message) {
        super(message);
    }
}
