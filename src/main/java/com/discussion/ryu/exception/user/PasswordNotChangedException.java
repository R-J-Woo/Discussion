package com.discussion.ryu.exception.user;

public class PasswordNotChangedException extends RuntimeException {

    public PasswordNotChangedException(String message) {
        super(message);
    }
}
