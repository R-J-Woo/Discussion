package com.discussion.ryu.exception;

import com.discussion.ryu.dto.ApiResponse;
import com.discussion.ryu.exception.discussion.DiscussionPostNotFoundException;
import com.discussion.ryu.exception.discussion.UserNotAuthorException;
import com.discussion.ryu.exception.discussion.VoteNotFoundException;
import com.discussion.ryu.exception.opinion.OpinionNotFoundException;
import com.discussion.ryu.exception.user.*;
import org.springframework.dao.DataIntegrityViolationException;
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
    @ExceptionHandler({
            AuthenticationFailedException.class,
            UserNotAuthorException.class
    })
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

    // 조회 실패 에러
    @ExceptionHandler({
            DiscussionPostNotFoundException.class,
            OpinionNotFoundException.class,
            VoteNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFoundException(RuntimeException e) {
        return ApiResponse.fail(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    // 중복 투표 에러
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        return ApiResponse.fail("이미 처리된 요청입니다.", HttpStatus.CONFLICT);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleRuntimeException(RuntimeException e) {
        return ApiResponse.fail("서버 내부 오류가 발생했습니다." + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
