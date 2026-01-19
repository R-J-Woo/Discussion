package com.discussion.ryu.exception.user;

public class DuplicateNameException extends RuntimeException {

    public DuplicateNameException(String message) {
        super(message);
    }
}
