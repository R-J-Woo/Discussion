package com.discussion.ryu.exception.discussion;

public class UserNotAuthorException extends RuntimeException {

    public UserNotAuthorException(String message) {
        super(message);
    }
}
