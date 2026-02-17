package com.discussion.ryu.exception.discussion;

public class VoteNotFoundException extends RuntimeException {

    public VoteNotFoundException(String message) {
        super(message);
    }
}
