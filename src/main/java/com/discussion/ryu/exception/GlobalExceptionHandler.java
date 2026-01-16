package com.discussion.ryu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleRuntimeException(RuntimeException e) {
        return "서버 내부 오류가 발생했습니다.";
    }

    @ExceptionHandler(AlreadyDeletedUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleAlreadyDeletedUserException(AlreadyDeletedUserException e) {
        return e.getMessage();
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String handleAuthenticationFailedException(AuthenticationFailedException e) {
        return e.getMessage();
    }

    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateNameException.class,
            DuplicateUsernameException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleDuplicateException(RuntimeException e) {
        return e.getMessage();
    }

    @ExceptionHandler({
            InvalidCurrentPasswordException.class,
            PasswordConfirmationMismatchException.class,
            PasswordNotChangedException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequestException(RuntimeException e) {
        return e.getMessage();
    }
}
