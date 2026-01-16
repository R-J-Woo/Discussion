package com.discussion.ryu.exception;

public class PasswordNotChangedException extends RuntimeException {

    public PasswordNotChangedException(String message) {
        super(message);
    }
}
