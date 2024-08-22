package com.findhomes.findhomesbe.exception.advice;

import com.findhomes.findhomesbe.DTO.SearchResponse;
import com.findhomes.findhomesbe.exception.exception.PreconditionRequiredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(PreconditionRequiredException.class)
    public ResponseEntity<SearchResponse> handleOptionalNotExistException(PreconditionRequiredException e) {
        log.error("[OptionalNotExistException]", e);
        return new ResponseEntity<>(new SearchResponse(false, 428, e.getMessage(), null), HttpStatus.PRECONDITION_REQUIRED);
    }
}
