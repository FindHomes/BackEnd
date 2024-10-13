package com.findhomes.findhomesbe.exception.advice;

import com.findhomes.findhomesbe.DTO.ErrorReponse;
import com.findhomes.findhomesbe.DTO.HouseDetailResponse;
import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.exception.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<SearchResponse> handleOptionalNotExistException(PreconditionRequiredException e) {
        log.error("[PreconditionRequiredException]", e);
        return new ResponseEntity<>(new SearchResponse(false, 428, e.getMessage(), null), HttpStatus.PRECONDITION_REQUIRED);
    }

    @ExceptionHandler(IllegalGptOutputException.class)
    public ResponseEntity<SearchResponse> handleIllegalGptOutputException(IllegalGptOutputException e) {
        return new ResponseEntity<>(new SearchResponse(
                false, 500, e.getMessage(), null
        ), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorReponse> handleUnauthorizedException(UnauthorizedException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 401, e.getMessage()
        ), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorReponse> handleDataNotFoundException(DataNotFoundException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 404, e.getMessage()
        ), HttpStatus.NOT_FOUND);
    }
    // 인수 오류 핸들러, 자바 IllgegalArgumentException과 구별하기 위해 접두사 Client 붙임
    @ExceptionHandler(ClientIllegalArgumentException.class)
    public ResponseEntity<ErrorReponse> handleClientIllegalArgumentException(DataNotFoundException e) {
        return new ResponseEntity<>(new ErrorReponse(
                false, 400, e.getMessage()
        ), HttpStatus.NOT_FOUND);
    }

}
