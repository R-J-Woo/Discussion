package com.discussion.ryu.exception.discussion;

public class DiscussionPostNotFoundException extends RuntimeException {

    public DiscussionPostNotFoundException(String message) {
        super(message);
    }
}
