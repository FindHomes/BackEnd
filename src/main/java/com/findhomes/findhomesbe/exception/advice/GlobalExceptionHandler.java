package com.findhomes.findhomesbe.exception.advice;

import com.findhomes.findhomesbe.DTO.Response;
import com.findhomes.findhomesbe.exception.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    // @PathVariable에 값이 없거나 잘못된 타입일 때 발생하는 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Response> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "잘못된 요청입니다. '" + ex.getName() + "'은(는) " + ex.getRequiredType().getSimpleName() + " 타입이어야 합니다.";
        return new ResponseEntity<>(new Response(
                false, 400, message
        ), HttpStatus.BAD_REQUEST);
    }

    // @PathVariable 값이 누락되었을 때 발생하는 예외 처리
    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<Response> handleMissingPathVariable(MissingPathVariableException ex) {
        String message = "경로 변수 '" + ex.getVariableName() + "'이(가) 누락되었습니다.";
        return new ResponseEntity<>(new Response(
                false, 400, message
        ), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<Response> handleOptionalNotExistException(PreconditionRequiredException e) {
        log.error("[PreconditionRequiredException]", e);
        return new ResponseEntity<>(new Response(
                false, 428, e.getMessage()
        ), HttpStatus.PRECONDITION_REQUIRED);
    }

    @ExceptionHandler(IllegalGptOutputException.class)
    public ResponseEntity<Response> handleIllegalGptOutputException(IllegalGptOutputException e) {
        return new ResponseEntity<>(new Response(
                false, 500, e.getMessage()
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Response> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<>(new Response(
                false, 401, e.getMessage()
        ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<Response> handleDataNotFoundException(DataNotFoundException e) {
        return new ResponseEntity<>(new Response(
                false, 404, e.getMessage()
        ), HttpStatus.NOT_FOUND);
    }
    // 인수 오류 핸들러, 자바 IllgegalArgumentException과 구별하기 위해 접두사 Client 붙임
    @ExceptionHandler(ClientIllegalArgumentException.class)
    public ResponseEntity<Response> handleClientIllegalArgumentException(DataNotFoundException e) {
        return new ResponseEntity<>(new Response(
                false, 400, e.getMessage()
        ), HttpStatus.NOT_FOUND);
    }

}
