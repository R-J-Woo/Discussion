package com.discussion.ryu.exception;

public class AlreadyDeletedUserException extends RuntimeException {

    public AlreadyDeletedUserException(String message) {
        super(message);
    }
}
