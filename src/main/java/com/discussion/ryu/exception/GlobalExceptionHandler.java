package com.discussion.ryu.exception;

import com.discussion.ryu.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AlreadyDeletedUserException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleAlreadyDeletedUserException(AlreadyDeletedUserException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.CONFLICT);
    }

    // 인증 실패 에러
    @ExceptionHandler(AuthenticationFailedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuthenticationFailedException(AuthenticationFailedException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // 중복 관련 에러
    @ExceptionHandler({
            DuplicateEmailException.class,
            DuplicateNameException.class,
            DuplicateUsernameException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicateException(RuntimeException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.CONFLICT);
    }

    // 비밀번호 관련 에러
    @ExceptionHandler({
            InvalidCurrentPasswordException.class,
            PasswordConfirmationMismatchException.class,
            PasswordNotChangedException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBadRequestException(RuntimeException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // @Valid 검증 실패 에러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidationException(MethodArgumentNotValidException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        return ApiResponse.fail("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
