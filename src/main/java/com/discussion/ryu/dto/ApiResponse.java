package com.discussion.ryu.dto;

import org.springframework.http.HttpStatus;

public record ApiResponse<T> (
    boolean success,
    int status,
    String message,
    T data
) {
    public static <T> ApiResponse<T> success(T data, String message, HttpStatus status) {
        return new ApiResponse<>(true, status.value(), message, data);
    }

    public static ApiResponse<Void> fail(String message, HttpStatus status) {
        return new ApiResponse<>(false, status.value(), message, null);
    }
}
