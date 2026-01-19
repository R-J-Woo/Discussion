package com.discussion.ryu.exception.user;

public class AlreadyDeletedUserException extends RuntimeException {

    public AlreadyDeletedUserException(String message) {
        super(message);
    }
}
